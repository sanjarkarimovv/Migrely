package com.migrely.app.designsystem.input

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.MigrelyInsets
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.awaitCancellation

/**
 * The room a screen with a text field gives the line being typed while the keyboard is up (BRD D192; ux R2, R3, R5, R6,
 * R10, R11). When the visible part under the top bar cannot hold that line, the bar steps aside until the keyboard is
 * gone ([TopBarAside]); when it holds the line but not its 8 dp margins, the fields keep the line whole and give the
 * margins what is left. One for each screen:
 * - [barModifier] on the top bar: its height follows the keyboard's own inset animation, with no curve of its own and
 *   in one step where the keyboard jumps, down to the status bar, which stays. Its content is clipped, never faded or
 *   drawn over the page, and while aside it leaves the accessibility tree and takes no keyboard focus.
 * - [scrollingPage] on the page in place of its `verticalScroll`: it scrolls the page with the given [ScrollState],
 *   measures the visible part, and while the keyboard grows keeps the focused field's line in view in the same layout
 *   pass (BRD D194).
 * - [ProvideTypingSpace] around the page, so its [CursorFollowingTextField]s and [SingleLineFieldInView]s report the
 *   line being typed, read the visible part and are kept in view.
 *
 * The page keeps its scroll offset as the bar comes back: it moves down by the bar's height and the page's
 * [KeyboardRoom] fills the rest, so closing the keyboard still never scrolls (BRD D189). Messages are laid out over the
 * whole screen, so the bar never moves them.
 */
@Stable
class TypingSpace internal constructor(
    private val keyboard: WindowInsets,
    private val keyboardSource: WindowInsets,
    private val keyboardTarget: WindowInsets,
    private val statusBar: WindowInsets,
    private val aboveKeyboard: Dp
) {
    internal val topBar = TopBarAside()
    private var page by mutableStateOf<Page?>(null)
    private var typed by mutableStateOf<TypedLine?>(null)

    // The focused field kept in view, and the keyboard's height at the page's last placement. Not state: the page's
    // layout reads them and must not be laid out again because they change.
    private var kept: KeptField? = null
    private var keyboardAtLastPlacement = -1
    private var visibleAtLastPlacement = -1

    // The clip first: it cuts the bar to the height the layout below reports, not to the bar's full height.
    private val barLayout: Modifier = Modifier
        .clipToBounds()
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val kept = statusBar.getTop(this).coerceAtMost(placeable.height)
            val gone = ((placeable.height - kept) * asideFraction()).roundToInt()
            layout(placeable.width, constraints.constrainHeight(placeable.height - gone)) {
                placeable.place(0, 0)
            }
        }

    /** Put on the screen's top bar. Read in composition: the bar's accessibility follows the decision. */
    val barModifier: Modifier
        get() = if (topBar.aside) barLayout.then(GoneForAssistiveTechnology) else barLayout

    private val pageRecord: Modifier = Modifier.onGloballyPositioned { coordinates ->
        val top = coordinates.positionInRoot().y.roundToInt()
        page =
            Page(top, top + coordinates.size.height, coordinates.findRootCoordinates().size.height)
    }

    /**
     * Put on the page in place of its `verticalScroll(scrollState)`: the page scrolls with [scrollState], and that same
     * state is the one kept in step with the keyboard, so the two can never differ (#259 review F3). A screen whose
     * scroll needs more (a TalkBack step, #255) adds it after this, on the same state.
     */
    fun scrollingPage(scrollState: ScrollState): Modifier =
        page(scrollState).verticalScroll(scrollState)

    /**
     * Right before the page's `verticalScroll([scrollState])`. While the keyboard grows, the focused field's
     * line is scrolled into the visible part within the layout pass that shrinks that part, before the page's content
     * is placed: no frame and no accessibility service ever sees the line under the keyboard, so TalkBack keeps its
     * focus on the field (BRD D194, #259). The line moves with the keyboard's own frames, with no curve of its own
     * (ux C1 to C3); a keyboard that shrinks or closes never scrolls the page (BRD D189).
     */
    private fun page(scrollState: ScrollState): Modifier =
        pageRecord.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val keyboardNow = keyboard.getBottom(this)
            layout(placeable.width, placeable.height) {
                // The page's content is still where the last placement put it, so the field's place is read against it.
                coordinates?.let { keepInView(it, placeable.height, keyboardNow, scrollState) }
                placeable.place(0, 0)
            }
        }

    /**
     * With the keyboard grown since the last placement and the kept field following it, scrolls [scrollState] so the
     * largest of the field's areas that fits [visible] shows in it (ux U1), measured against [page], the visible part.
     * A field not followed that the keyboard is about to hide ([keyboardHides]) is handed back to its follow first.
     */
    private fun keepInView(
        page: LayoutCoordinates,
        visible: Int,
        keyboardNow: Int,
        scrollState: ScrollState
    ) {
        val grew = keyboardAtLastPlacement in 0 until keyboardNow
        val visibleBefore = visibleAtLastPlacement
        keyboardAtLastPlacement = keyboardNow
        visibleAtLastPlacement = visible
        if (!grew) return
        Snapshot.withoutReadObservation {
            val field = kept ?: return@withoutReadObservation
            val coordinates =
                field.coordinates()?.takeIf { it.isAttached } ?: return@withoutReadObservation
            val fieldTop = page.localPositionOf(coordinates, Offset.Zero).y
            val fieldHeight = coordinates.size.height.toFloat()
            val areas = field.areas(visible)
            val area =
                areas.firstOrNull { it.height <= visible } ?: areas.lastOrNull()
                    ?: return@withoutReadObservation
            if (!field.follow.isFollowing) {
                // Not followed (the person moved the page), but the keyboard must not hide the focused field: out of
                // view it leaves the accessibility tree and TalkBack moves its focus to whatever the next double tap
                // then sets (#259, decision (a')). From the frame that would leave less than its line showing, the
                // field follows again, under the follow's own rules: a scroll away ends it.
                val line = areas.last().height
                if (!keyboardHides(fieldTop, fieldHeight, line, visibleBefore, visible)) {
                    return@withoutReadObservation
                }
                field.follow.onBroughtIntoView()
            }
            val top = page.localPositionOf(coordinates, area.topLeft).y
            var shift = scrollToShow(top, top + area.height, visible)
            // A focused field wholly in view before the visible part shrank is one Compose's own scroll brings back
            // once clipped, animated, after the keyboard has stopped. Land where it would, so it has nothing to do
            // (ux C3); either way it lines up an edge of the field, so the line inside stays whole.
            if (visibleBefore > 0 &&
                focusedChildScroll(fieldTop, fieldHeight, visibleBefore) == 0f
            ) {
                shift += focusedChildScroll(fieldTop - shift, fieldHeight, visible)
            }
            if (shift > 0f) scrollState.dispatchRawDelta(shift)
        }
    }

    /** Until cancelled, [field] is the focused field the page keeps in view as the keyboard grows. */
    internal suspend fun keepInView(field: KeptField) {
        kept = field
        try {
            awaitCancellation()
        } finally {
            if (kept === field) kept = null
        }
    }

    /** How far the bar has stepped aside, from 0 (there) to 1 (gone), as the keyboard's own animation goes. */
    private fun Density.asideFraction(): Float {
        if (!topBar.aside) return 0f
        // The keyboard's height before or after this animation, whichever is lower but not 0: the bar moves only while
        // the keyboard opens or closes, never while it changes height (symbols, emoji).
        val up = listOf(keyboardSource.getBottom(this), keyboardTarget.getBottom(this))
            .filter { it > 0 }
            .minOrNull() ?: return 0f
        return (keyboard.getBottom(this).toFloat() / up).coerceIn(0f, 1f)
    }

    /** Decides the bar, with every value read again on each change, until cancelled. */
    internal suspend fun decide(density: Density) {
        val aboveKeyboardPx = with(density) { aboveKeyboard.roundToPx() }
        snapshotFlow {
            val finalKeyboard = keyboardTarget.getBottom(density)
            val visible = page?.let { page ->
                // The keyboard only takes the visible part from below.
                minOf(page.bottom, page.root - finalKeyboard - aboveKeyboardPx) - page.top
            }
            Decision(finalKeyboard, keyboard.getBottom(density), visible, typed?.height)
        }.collect { topBar.update(it.finalKeyboard, it.keyboard, it.visible, it.line) }
    }

    /** Until cancelled, the focused field's line being typed is [height]'s, in px, when known. */
    internal suspend fun reportLine(height: () -> Float?) {
        val line = TypedLine.Owner()
        try {
            snapshotFlow(height).collect { px ->
                if (px != null) typed = TypedLine(line, px.roundToInt())
            }
        } finally {
            if (typed?.owner === line) typed = null
        }
    }

    /**
     * The margin to keep above and below a line [line] px tall, up to [margin]: what the visible part has left once the
     * line is whole (ux R2). Where even the line does not fit, none.
     */
    internal fun marginAround(line: Float, margin: Float): Float {
        val visible = page?.let { it.bottom - it.top } ?: return margin
        return marginAround(line, margin, visible)
    }

    /** As [marginAround], for a visible part [visible] px tall. */
    internal fun marginAround(line: Float, margin: Float, visible: Int): Float =
        (visible - line).coerceIn(0f, margin)

    private class Page(val top: Int, val bottom: Int, val root: Int)

    private class TypedLine(val owner: Owner, val height: Int) {
        class Owner
    }

    private data class Decision(
        val finalKeyboard: Int,
        val keyboard: Int,
        val visible: Int?,
        val line: Int?
    )
}

/**
 * A focused field a [TypingSpace] keeps in view while the keyboard grows: where it is, whether it follows the keyboard
 * ([KeyboardFollow]), and its areas in its own coordinates for a visible part of a given height, the largest first.
 */
internal class KeptField(
    val coordinates: () -> LayoutCoordinates?,
    val follow: KeyboardFollow,
    val areas: (visible: Int) -> List<Rect>
)

/**
 * How far a page must scroll for an area from [top] to [bottom], in px from the top of a visible part [visible] px
 * tall, to show in it (BRD D194, design D2): 0 where it shows, the distance below or above, its top first where it is
 * taller than the part.
 */
internal fun scrollToShow(top: Float, bottom: Float, visible: Int): Float = when {
    bottom > visible -> minOf(bottom - visible, top)
    top < 0f -> top
    else -> 0f
}

/**
 * How far Compose's own scroll moves a focused field from [top], [size] px tall, into a visible part [visible] px tall:
 * Compose 1.9's default bring-into-view distance (`BringIntoViewSpec`), which its scrolling container uses to keep a
 * focused child in view as the container shrinks. 0 where the field shows whole, or covers the whole part.
 */
internal fun focusedChildScroll(top: Float, size: Float, visible: Int): Float {
    val bottom = top + size
    return when {
        top >= 0f && bottom <= visible -> 0f
        top < 0f && bottom > visible -> 0f
        abs(top) < abs(bottom - visible) -> top
        else -> bottom - visible
    }
}

/**
 * The least of a field that shows, in px: Compose rounds a node's clipped bounds to whole pixels and drops a node whose
 * rounded bounds are empty from the accessibility tree, which a sliver under one pixel can be.
 */
internal const val MIN_SHOWN_PX = 1f

/** How much of a field [size] px tall at [top] shows in a visible part [visible] px tall, in px. */
internal fun shownInPage(top: Float, size: Float, visible: Int): Float =
    (minOf(top + size, visible.toFloat()) - maxOf(top, 0f)).coerceAtLeast(0f)

/**
 * Whether a keyboard that shrinks the visible part from [visibleBefore] to [visible] px is about to hide a field [size]
 * px tall at [top] whose line is [line] px tall: the field showed ([MIN_SHOWN_PX]), the keyboard takes some of it, and
 * less than its line would be left. A field the person had already scrolled wholly away is not one.
 */
internal fun keyboardHides(
    top: Float,
    size: Float,
    line: Float,
    visibleBefore: Int,
    visible: Int
): Boolean {
    val before = shownInPage(top, size, visibleBefore)
    val now = shownInPage(top, size, visible)
    return before >= MIN_SHOWN_PX && now < before && now < line
}

/** Gone from TalkBack, Switch Access and Tab: a bar that is not on screen is never a stop. */
private val GoneForAssistiveTechnology: Modifier = Modifier
    .clearAndSetSemantics {}
    .focusProperties { onEnter = { cancelFocusChange() } }
    .focusGroup()

internal val LocalTypingSpace = staticCompositionLocalOf<TypingSpace?> { null }

/**
 * The [TypingSpace] of a screen whose page's visible part ends at the keyboard's top, or [aboveKeyboard] above it
 * where something the page does not scroll lies between them (Delete all data's divider).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun rememberTypingSpace(aboveKeyboard: Dp = 0.dp): TypingSpace {
    val density = LocalDensity.current
    // The insets objects, not their values: the keyboard's frames do not recompose the screen.
    val keyboard = WindowInsets.ime
    val source = WindowInsets.imeAnimationSource
    val target = WindowInsets.imeAnimationTarget
    val statusBar = MigrelyInsets.bars
    val space = remember(keyboard, source, target, statusBar, aboveKeyboard) {
        TypingSpace(keyboard, source, target, statusBar, aboveKeyboard)
    }
    LaunchedEffect(space, density) { space.decide(density) }
    return space
}

/** Lets the fields in [content] use [space]. */
@Composable
fun ProvideTypingSpace(space: TypingSpace, content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalTypingSpace provides space, content = content)
