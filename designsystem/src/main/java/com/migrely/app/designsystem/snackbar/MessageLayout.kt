package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth

private const val TOP_BAR = "topBar"
private const val MESSAGE = "message"
private const val CONTENT = "content"
private const val PARK = "park"

/**
 * A screen that shows messages, in the order keyboard, Switch Access and TalkBack reach it: [topBar]'s controls, then
 * [message] (a [MigrelySnackbarHost]), then [content] (BRD D178 (4), EC-UX-36). Compose's Tab search follows the order
 * children are placed and TalkBack follows `traversalIndex`, so both come from this one layout, not from each screen.
 *
 * Drawing stays as before: [topBar] at the top and [content] below it, as in a `Column`, and [message] measured over the
 * whole screen and drawn above both, as a later sibling in a `Box` was, so its place and size do not change. With no
 * top bar, as on Home and onboarding, the message is the first stop.
 */
@Composable
fun MessageLayout(
    message: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val park = remember { MessageFocusPark() }
    Layout(
        content = {
            FocusPark(park)
            Slot(TOP_BAR, order = 0f, topBar)
            CompositionLocalProvider(LocalMessageFocusPark provides park) {
                Slot(MESSAGE, order = 1f, message)
            }
            Slot(CONTENT, order = 2f, content)
        },
        modifier = modifier.semantics { isTraversalGroup = true }
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val parked = measurables.first { it.layoutId == PARK }.measure(Constraints())
        val topBar = measurables.first { it.layoutId == TOP_BAR }.measure(loose)
        val below = if (loose.hasBoundedHeight) {
            loose.copy(maxHeight = (loose.maxHeight - topBar.height).coerceAtLeast(0))
        } else {
            loose
        }
        val content = measurables.first { it.layoutId == CONTENT }.measure(below)
        val message = measurables.first { it.layoutId == MESSAGE }.measure(loose)
        val width = constraints.constrainWidth(maxOf(topBar.width, content.width, message.width))
        val height = constraints.constrainHeight(
            maxOf(topBar.height + content.height, message.height)
        )
        layout(width, height) {
            // Placed in reading order, so Tab follows it; the message is drawn above the content.
            parked.placeRelative(0, 0)
            topBar.placeRelative(0, 0)
            message.placeRelative(0, 0, zIndex = 1f)
            content.placeRelative(0, topBar.height)
        }
    }
}

@Composable
private fun Slot(id: String, order: Float, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .layoutId(id)
            .semantics {
                isTraversalGroup = true
                traversalIndex = order
            }
    ) { content() }
}

/**
 * Where focus waits when the focused message leaves or is replaced (ux Q3, F8): a target before the top bar with no
 * semantics and no action, focusable only while it holds focus. It looks like nothing is focused, Enter and Space do
 * nothing, Tab goes to the screen's first stop and Shift+Tab to its last.
 */
internal class MessageFocusPark {
    val requester = FocusRequester()
    var parked = false

    /** Moves focus here; false when that was not possible. */
    fun park(): Boolean {
        parked = true
        parked = requester.requestFocus()
        return parked
    }
}

internal val LocalMessageFocusPark = staticCompositionLocalOf<MessageFocusPark?> { null }

@Composable
private fun FocusPark(park: MessageFocusPark) {
    Box(
        Modifier
            .layoutId(PARK)
            .focusRequester(park.requester)
            .focusProperties { canFocus = park.parked }
            .onFocusChanged { if (!it.isFocused) park.parked = false }
            .focusTarget()
    )
}
