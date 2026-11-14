package com.migrely.app.designsystem.snackbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.input.TapGuard
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlin.math.roundToInt

private const val ENTER_MILLIS = 250

/**
 * The alpha a message fades in from: below one visible step on its dim surface, and never 0, which Android's
 * accessibility tree treats as invisible (BRD D178 (5), ux Q4).
 */
internal const val ENTER_START_ALPHA = 0.02f
private const val EXIT_MILLIS = 200

// How long a message's place keeps swallowing touches after it changes: past the slide in or fade out, plus the time a
// finger already on its way takes to land (QA #52).
private const val GUARD_MILLIS = 800L
private const val PREVIEW_DURATION_MILLIS = 5_000L
private const val TIME_LEFT_ALPHA = 0.45f
private const val INLINE_MAX_LINES = 2
private val MAX_WIDTH = 560.dp
private val ICON_SIZE = 20.dp
private val START_PADDING = 16.dp
private val END_PADDING = 4.dp
private val ICON_GAP = 12.dp
private val TEXT_VERTICAL_PADDING = 12.dp
private val MIN_HEIGHT = 52.dp
private val TIME_LEFT_STROKE = 2.dp

/**
 * [MigrelySnackbarHost]'s top offset on a screen with a top bar: with the host's own 8 dp it sets a message's top edge at
 * the bottom of Material's default 64 dp bar, so the bar's controls stay uncovered (BRD D158).
 */
val MESSAGE_OFFSET_BELOW_TOP_BAR = 56.dp

/**
 * A short message at the top of the screen (BRD EC-UX-16): dim, with an icon whose shape tells the [tone], and either
 * an [action] or a close button. It can be swiped away sideways. [timeLeft] returns the fraction of time left (1 to 0)
 * and draws a thin line along the bottom edge; pass null to hide it (reduce motion, or no time limit).
 *
 * [baseDurationMillis] is the message's own duration, null when it stays until closed. A tap on a message with no
 * [action] and a duration dismisses it, by touch only, so screen readers still find the close button and nothing new. A
 * tap cannot take away the offer to undo, or a message with no duration, which says the last action was not saved
 * (BRD D158).
 *
 * TalkBack reads an error at once and anything else politely. [announcePolitely] makes an error polite too, so it waits
 * for an assertive announcement already running, such as the first-10 safety card (BRD D165 (3)).
 */
@Suppress("LongParameterList")
@Composable
fun MigrelySnackbar(
    text: String,
    tone: SnackbarTone,
    action: SnackbarAction?,
    baseDurationMillis: Long?,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    timeLeft: (() -> Float)? = null,
    announcePolitely: Boolean = false
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val swipeState = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue != SwipeToDismissBoxValue.Settled) currentOnDismiss()
    }
    val bodyTap = if (action == null && baseDurationMillis != null) {
        Modifier.pointerInput(Unit) { detectTapGestures { currentOnDismiss() } }
    } else {
        Modifier
    }
    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {},
        modifier = modifier.then(bodyTap)
    ) {
        SnackbarSurface(text, tone, action, dismissLabel, onDismiss, timeLeft, announcePolitely)
    }
}

@Composable
private fun SnackbarSurface(
    text: String,
    tone: SnackbarTone,
    action: SnackbarAction?,
    dismissLabel: String,
    onDismiss: () -> Unit,
    timeLeft: (() -> Float)?,
    announcePolitely: Boolean
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = TIME_LEFT_ALPHA)
    val liveRegionMode = if (tone == SnackbarTone.Error && !announcePolitely) {
        LiveRegionMode.Assertive
    } else {
        LiveRegionMode.Polite
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = liveRegionMode }
    ) {
        SnackbarLayout(
            // Inside the surface, so the rounded shape clips the line at the corners.
            modifier = Modifier.drawWithContent {
                drawContent()
                // Read in the draw phase only, so the countdown redraws without recomposing.
                val fraction = timeLeft?.invoke()?.coerceIn(0f, 1f) ?: return@drawWithContent
                val y = size.height - TIME_LEFT_STROKE.toPx() / 2
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width * fraction, y),
                    strokeWidth = TIME_LEFT_STROKE.toPx()
                )
            },
            icon = {
                Icon(
                    imageVector = SnackbarIcons.forTone(tone),
                    contentDescription = null,
                    tint = tone.color(),
                    modifier = Modifier.size(ICON_SIZE)
                )
            },
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = TEXT_VERTICAL_PADDING)
                )
            },
            action = { SnackbarEndControl(action, dismissLabel, onDismiss) }
        )
    }
}

@Composable
private fun SnackbarEndControl(
    action: SnackbarAction?,
    dismissLabel: String,
    onDismiss: () -> Unit
) {
    if (action != null) {
        TextButton(
            onClick = action.onClick,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics { contentDescription = action.spokenLabel }
        ) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = SnackbarIcons.Close,
                contentDescription = dismissLabel,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ICON_SIZE)
            )
        }
    }
}

@Composable
private fun SnackbarTone.color(): Color = when (this) {
    SnackbarTone.Confirmation -> MigrelyColors.Positive
    SnackbarTone.Information -> MaterialTheme.colorScheme.secondary
    SnackbarTone.Error -> MaterialTheme.colorScheme.error
}

/**
 * Places the action beside the text when the text then fits in two lines; otherwise (large fonts, long translations)
 * the action moves below the text, aligned to the end, so the text is never squeezed or cut off (BRD EC-UX-03).
 */
@Composable
private fun SnackbarLayout(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    action: @Composable () -> Unit
) {
    Layout(contents = listOf(icon, text, action), modifier = modifier) { measurables, constraints ->
        val (iconMeasurables, textMeasurables, actionMeasurables) = measurables
        val startPadding = START_PADDING.roundToPx()
        val endPadding = END_PADDING.roundToPx()
        val gap = ICON_GAP.roundToPx()
        val textPadding = 2 * TEXT_VERTICAL_PADDING.roundToPx()
        val width = constraints.maxWidth
        val iconPlaceable = iconMeasurables.first().measure(Constraints())
        val actionPlaceable = actionMeasurables.first().measure(Constraints(maxWidth = width))
        val textMeasurable = textMeasurables.first()
        val besideIcon = width - startPadding - iconPlaceable.width - gap - endPadding
        val inlineTextWidth = besideIcon - actionPlaceable.width
        // Count lines from intrinsic heights; rounding makes a direct height comparison unreliable.
        val lineHeight = (
            textMeasurable.minIntrinsicHeight(
                Constraints.Infinity
            ) - textPadding
            ).coerceAtLeast(1)
        val inline = inlineTextWidth > 0 &&
            (
                (textMeasurable.minIntrinsicHeight(inlineTextWidth) - textPadding).toFloat() /
                    lineHeight
                )
                .roundToInt() <= INLINE_MAX_LINES
        val textPlaceable = textMeasurable.measure(
            Constraints(maxWidth = (if (inline) inlineTextWidth else besideIcon).coerceAtLeast(0))
        )
        val inlineActionHeight = if (inline) actionPlaceable.height else 0
        val firstRowHeight = maxOf(textPlaceable.height, inlineActionHeight, MIN_HEIGHT.roundToPx())
        val height = if (inline) firstRowHeight else firstRowHeight + actionPlaceable.height
        val actionX = width - endPadding - actionPlaceable.width
        layout(width, height) {
            iconPlaceable.place(startPadding, (firstRowHeight - iconPlaceable.height) / 2)
            textPlaceable.place(
                startPadding + iconPlaceable.width + gap,
                (firstRowHeight - textPlaceable.height) / 2
            )
            if (inline) {
                actionPlaceable.place(actionX, (firstRowHeight - actionPlaceable.height) / 2)
            } else {
                actionPlaceable.place(actionX, firstRowHeight)
            }
        }
    }
}

/**
 * Shows [message] at the top of the screen, inside the status bar and display cutout and at most 560 dp wide. A message
 * that arrives when none shows slides and fades in over 250 ms, and one that leaves fades out over 200 ms; with
 * [reduceMotion] both happen at once (BRD EC-UX-07, EC-UX-08).
 *
 * A message that replaces another one takes its place in the same frame, with no cross-fade (BRD D178 (5)). The arrival
 * fade starts at [ENTER_START_ALPHA], never 0: Android's accessibility tree leaves out a fully transparent node, so a
 * live region added in that frame was announced with no text (BRD D165 (3), QA TB12, #183). Each message must be its
 * own value, for example with the time it was posted, so a retry with the same text is a new node and is spoken again.
 *
 * [onMessageBottomChanged] gets the bottom of the message in root coordinates (`positionInRoot`), or 0 dp while none
 * shows, as it moves; a screen measures its own content the same way to keep it clear of the message (BRD D52). The
 * area grows with the message's slide in and shrinks only once its fade out ends, so what follows it never slides under
 * a message still on screen. After a replacement it moves down at once when the new message is taller, and eases up
 * over 250 ms when it is shorter (ux F1).
 *
 * [topOffset] moves the message down, past a top bar for example, but never so far that the message leaves the safe
 * area: one too tall for its place rises just enough, never above the safe top plus 8 dp (BRD D178 (6)). [guardMillis]
 * is how long its place keeps swallowing touches after it changes, and after a replacement that place is the taller of
 * the two messages (BRD D158, ux F2).
 *
 * A leaving message is out of the focus order and the semantics tree from its first exit frame, and focus on it clears
 * when it leaves or is replaced, never moving on to another control (BRD D178 (4), ux F8). Put the host in the message
 * slot of [MessageLayout] for the reading order.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun <T : Any> MigrelySnackbarHost(
    message: T?,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    topOffset: Dp = 0.dp,
    guardMillis: Long = GUARD_MILLIS,
    onMessageBottomChanged: (Dp) -> Unit = {},
    content: @Composable (T) -> Unit
) {
    val density = LocalDensity.current
    val guard = remember { ReplacementGuard() }
    val bottom = remember { MessageBottom() }
    guard.target(message)
    bottom.target(message)
    val focus = rememberMessageFocus(message)
    val currentOnMessageBottomChanged by rememberUpdatedState(onMessageBottomChanged)
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
            // The bars and the cutout bound it, not the keyboard: an open keyboard must not lift a message (review 9.2 (d)).
            .fitTopOffset(topOffset, WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Below the message, so the message itself stays tappable at once, but above what the screen draws under it:
        // a tap aimed at a message that has just gone, or is still arriving, never reaches a top bar's action
        // underneath (QA #52).
        TapGuard(
            key = message ?: NoMessage,
            durationMillis = guardMillis,
            modifier = Modifier
                .widthIn(max = MAX_WIDTH)
                .fillMaxWidth()
                .height(guard.height)
        )
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                when {
                    // No size animation either, so what follows the message's bottom moves at once too.
                    reduceMotion -> EnterTransition.None togetherWith ExitTransition.None using null
                    // In place: only the area changes, at once when it grows, gently when it shrinks (ux F1).
                    initialState != null && targetState != null ->
                        EnterTransition.None togetherWith ExitTransition.None using
                            SizeTransform(clip = false) { initial, target ->
                                if (target.height < initial.height) tween(ENTER_MILLIS) else snap()
                            }
                    else -> (
                        slideInVertically(tween(ENTER_MILLIS)) { -it / 2 } +
                            fadeIn(tween(ENTER_MILLIS), initialAlpha = ENTER_START_ALPHA)
                        ) togetherWith
                        fadeOut(tween(EXIT_MILLIS)) using
                        SizeTransform(clip = false) { initial, target ->
                            if (target.height < initial.height) {
                                tween(ENTER_MILLIS, delayMillis = EXIT_MILLIS)
                            } else {
                                tween(ENTER_MILLIS)
                            }
                        }
                }
            },
            // Every message shares one slot, so a replacement is no transition at all: key() below swaps the node
            // within one frame, and the new one is never transparent.
            contentKey = { shown -> if (shown != null) InPlace else null },
            contentAlignment = Alignment.TopCenter,
            label = "snackbar",
            modifier = Modifier.onGloballyPositioned { coordinates ->
                bottom.area(coordinates.positionInRoot().y, coordinates.size.height)
                currentOnMessageBottomChanged(with(density) { bottom.value.toDp() })
            }
        ) { shown ->
            if (shown != null) {
                val leaving = transition.targetState == EnterExitState.PostExit
                Box(
                    modifier = Modifier
                        .widthIn(max = MAX_WIDTH)
                        // Measured at full size from the first frame, while the slide only moves it.
                        .onSizeChanged { size ->
                            if (size.height >
                                0
                            ) {
                                guard.measured(shown, with(density) { size.height.toDp() })
                            }
                            if (bottom.measured(shown, size.height)) {
                                currentOnMessageBottomChanged(with(density) { bottom.value.toDp() })
                            }
                        }
                        .then(focus.modifier(leaving))
                ) { key(shown) { content(shown) } }
            }
        }
    }
}

@Composable
private fun SnackbarPreviewColumn() {
    MigrelyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MigrelySnackbar(
                text = "Migraine started",
                tone = SnackbarTone.Confirmation,
                action = SnackbarAction("Undo", "Undo the start") {},
                baseDurationMillis = PREVIEW_DURATION_MILLIS,
                dismissLabel = "Dismiss",
                onDismiss = {},
                timeLeft = { 0.6f }
            )
            MigrelySnackbar(
                text = "Your previous migraine ended at 3:56 PM.",
                tone = SnackbarTone.Information,
                action = null,
                baseDurationMillis = PREVIEW_DURATION_MILLIS,
                dismissLabel = "Dismiss",
                onDismiss = {},
                modifier = Modifier.padding(top = 12.dp)
            )
            MigrelySnackbar(
                text = "Couldn't save. Your phone's storage is full.",
                tone = SnackbarTone.Error,
                action = null,
                baseDurationMillis = null,
                dismissLabel = "Dismiss",
                onDismiss = {},
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(
    name = "Snackbar tones",
    widthDp = 360,
    backgroundColor = 0xFF0F1115,
    showBackground = true
)
@Composable
private fun SnackbarTonesPreview() = SnackbarPreviewColumn()

@Preview(
    name = "Snackbar tones at 200%",
    widthDp = 360,
    fontScale = 2f,
    backgroundColor = 0xFF0F1115,
    showBackground = true
)
@Composable
private fun SnackbarLargeFontPreview() = SnackbarPreviewColumn()

/** The guard's key while no message shows, so a message leaving re-arms it like one arriving. */
private object NoMessage

/** The one content key of every message shown in place. */
private object InPlace
