package com.migrely.app.designsystem.snackbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlin.math.roundToInt

private const val ENTER_MILLIS = 250
private const val EXIT_MILLIS = 200
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
 * A short message at the top of the screen (BRD EC-UX-16): dim, with an icon whose shape tells the [tone], and either
 * an [action] or a close button. It can be swiped away sideways. [timeLeft] returns the fraction of time left (1 to 0)
 * and draws a thin line along the bottom edge; pass null to hide it (reduce motion, or no time limit).
 */
@Composable
fun MigrelySnackbar(
    text: String,
    tone: SnackbarTone,
    action: SnackbarAction?,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    timeLeft: (() -> Float)? = null
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val swipeState = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue != SwipeToDismissBoxValue.Settled) currentOnDismiss()
    }
    SwipeToDismissBox(state = swipeState, backgroundContent = {}, modifier = modifier) {
        SnackbarSurface(text, tone, action, dismissLabel, onDismiss, timeLeft)
    }
}

@Composable
private fun SnackbarSurface(
    text: String,
    tone: SnackbarTone,
    action: SnackbarAction?,
    dismissLabel: String,
    onDismiss: () -> Unit,
    timeLeft: (() -> Float)?
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = TIME_LEFT_ALPHA)
    val liveRegionMode = if (tone ==
        SnackbarTone.Error
    ) {
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
 * Shows [message] at the top of the screen, inside the status bar and display cutout and at most 560 dp wide. A new
 * message replaces the old one. Messages slide and fade in over 250 ms and fade out over 200 ms, or switch at once with
 * [reduceMotion] (BRD EC-UX-07, EC-UX-08).
 */
@Composable
fun <T : Any> MigrelySnackbarHost(
    message: T?,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                if (reduceMotion) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (
                        slideInVertically(tween(ENTER_MILLIS)) { -it / 2 } +
                            fadeIn(tween(ENTER_MILLIS))
                        ) togetherWith
                        fadeOut(tween(EXIT_MILLIS)) using SizeTransform(clip = false)
                }
            },
            contentAlignment = Alignment.TopCenter,
            label = "snackbar"
        ) { shown ->
            if (shown != null) {
                Box(modifier = Modifier.widthIn(max = MAX_WIDTH)) { content(shown) }
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
                dismissLabel = "Dismiss",
                onDismiss = {},
                timeLeft = { 0.6f }
            )
            MigrelySnackbar(
                text = "Your previous migraine ended at 3:56 PM.",
                tone = SnackbarTone.Information,
                action = null,
                dismissLabel = "Dismiss",
                onDismiss = {},
                modifier = Modifier.padding(top = 12.dp)
            )
            MigrelySnackbar(
                text = "Couldn't save. Your phone's storage is full.",
                tone = SnackbarTone.Error,
                action = null,
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
