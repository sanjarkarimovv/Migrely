package com.migrely.app.designsystem.dialog

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.PageScroll
import com.migrely.app.designsystem.motion.rememberReduceMotion

/**
 * An alert dialog's title and content in one area that scrolls, for the dialog's `text` slot with no `title` (T23 8.1
 * rows R03, R04). Material's title slot does not scroll: at large fonts in a short window a long title, or stacked
 * buttons, left the content no height at all. Here the title scrolls away before the content is cut. It keeps the
 * dialog title's style, is a heading, and names the pane for TalkBack. [modifier] applies to the visible area, before
 * the scroll.
 *
 * While more lies below or above, a soft fade in the dialog's colour shows it at that edge (ux F6): a person in a
 * migraine does not explore a dialog for hidden controls. It eases in and out, at once with reduced motion, draws over
 * the content only, and has no semantics.
 */
@Composable
fun ScrollingDialogContent(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val fades = edgeFades(scrollState.canScrollBackward, scrollState.canScrollForward)
    val fadeSpec: AnimationSpec<Float> = if (rememberReduceMotion()) snap() else tween(FADE_MILLIS)
    val topFade by animateFloatAsState(
        targetValue = if (fades.top) 1f else 0f,
        animationSpec = fadeSpec,
        label = "topFade"
    )
    val bottomFade by animateFloatAsState(
        targetValue = if (fades.bottom) 1f else 0f,
        animationSpec = fadeSpec,
        label = "bottomFade"
    )
    val surface = AlertDialogDefaults.containerColor
    PageScroll {
        // The top padding leaves room for the title's tall letters, drawn above its line at large fonts.
        Column(
            modifier
                .drawWithContent {
                    drawContent()
                    val fade = FADE_HEIGHT.toPx().coerceAtMost(size.height / 2)
                    if (topFade > 0f) {
                        drawRect(
                            Brush.verticalGradient(
                                listOf(surface.copy(alpha = topFade), Color.Transparent),
                                endY = fade
                            ),
                            size = Size(size.width, fade)
                        )
                    }
                    if (bottomFade > 0f) {
                        drawRect(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, surface.copy(alpha = bottomFade)),
                                startY = size.height - fade,
                                endY = size.height
                            ),
                            topLeft = Offset(0f, size.height - fade),
                            size = Size(size.width, fade)
                        )
                    }
                }
                .pageScroll(scrollState)
                .padding(top = 4.dp)
                .semantics { paneTitle = title }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = AlertDialogDefaults.titleContentColor,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .semantics { heading() }
            )
            content()
        }
    }
}

/** Which edges of a scrolling dialog area show a fade: the ones with more content beyond them. */
internal data class EdgeFades(val top: Boolean, val bottom: Boolean)

internal fun edgeFades(canScrollBackward: Boolean, canScrollForward: Boolean) =
    EdgeFades(top = canScrollBackward, bottom = canScrollForward)

private const val FADE_MILLIS = 150
private val FADE_HEIGHT = 24.dp
