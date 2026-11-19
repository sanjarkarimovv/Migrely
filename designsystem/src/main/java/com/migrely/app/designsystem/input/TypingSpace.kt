package com.migrely.app.designsystem.input

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.focusProperties
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
import kotlin.math.roundToInt

/**
 * The room a screen with a text field gives the line being typed while the keyboard is up (BRD D192; ux R2, R3, R5, R6,
 * R10, R11). When the visible part under the top bar cannot hold that line, the bar steps aside until the keyboard is
 * gone ([TopBarAside]); when it holds the line but not its 8 dp margins, the fields keep the line whole and give the
 * margins what is left. One for each screen:
 * - [barModifier] on the top bar: its height follows the keyboard's own inset animation, with no curve of its own and
 *   in one step where the keyboard jumps, down to the status bar, which stays. Its content is clipped, never faded or
 *   drawn over the page, and while aside it leaves the accessibility tree and takes no keyboard focus.
 * - [pageModifier] on the page, right before its `verticalScroll`: what it measures is the visible part.
 * - [ProvideTypingSpace] around the page, so its [CursorFollowingTextField]s and [SingleLineFieldInView]s report the
 *   line being typed and read the visible part.
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

    /** Put on the page right before its `verticalScroll`. */
    val pageModifier: Modifier = Modifier.onGloballyPositioned { coordinates ->
        val top = coordinates.positionInRoot().y.roundToInt()
        page =
            Page(top, top + coordinates.size.height, coordinates.findRootCoordinates().size.height)
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
     * How far the page must scroll, in px, for [area] in [coordinates] to show in the visible part (BRD D194, design
     * D2): 0 where it shows, its top first where it is taller than that part, null while the page is unknown or
     * [coordinates] are detached. Both come from the latest layout, the one with the bar's current height, so a bar
     * stepping aside in the same frame is never counted twice (ux C2).
     */
    internal fun scrollToShow(coordinates: LayoutCoordinates, area: Rect): Float? {
        val page = page ?: return null
        if (!coordinates.isAttached) return null
        val top = coordinates.localToRoot(area.topLeft).y
        val bottom = coordinates.localToRoot(area.bottomLeft).y
        return when {
            bottom > page.bottom -> minOf(bottom - page.bottom, top - page.top)
            top < page.top -> top - page.top
            else -> 0f
        }
    }

    /**
     * The margin to keep above and below a line [line] px tall, up to [margin]: what the visible part has left once the
     * line is whole (ux R2). Where even the line does not fit, none.
     */
    internal fun marginAround(line: Float, margin: Float): Float {
        val visible = page?.let { it.bottom - it.top } ?: return margin
        return (visible - line).coerceIn(0f, margin)
    }

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
