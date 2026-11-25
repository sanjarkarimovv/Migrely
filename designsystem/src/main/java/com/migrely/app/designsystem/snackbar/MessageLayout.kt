package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import com.migrely.app.designsystem.input.ProvideTypingSpace
import com.migrely.app.designsystem.input.TypingSpace

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
 *
 * [keyboardMessageLast] places the message after the content for Tab only, for a screen that links the message into
 * its content itself (Home Today, BRD D178 (4), ux F10). TalkBack's order, the drawing and the place do not change.
 *
 * [typingSpace], for a screen with a text field, lets [topBar] step aside while the line being typed cannot fit under it
 * and gives [content]'s fields the space (BRD D192). The message keeps its place: it is measured over the whole screen.
 */
@Composable
fun MessageLayout(
    message: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    keyboardMessageLast: Boolean = false,
    typingSpace: TypingSpace? = null,
    content: @Composable () -> Unit
) {
    val park = remember { MessageFocusPark() }
    val cover = remember { MessageCover() }
    Layout(
        content = {
            FocusPark(park)
            Slot(TOP_BAR, order = 0f, typingSpace?.barModifier ?: Modifier, topBar)
            // The content may park focus too ([rememberFocusParking], BRD D181 (6)).
            CompositionLocalProvider(
                LocalMessageFocusPark provides park,
                LocalMessageCover provides cover
            ) {
                Slot(MESSAGE, order = 1f, content = message)
                Slot(CONTENT, order = 2f) {
                    if (typingSpace == null) content() else ProvideTypingSpace(typingSpace, content)
                }
            }
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
            if (!keyboardMessageLast) message.placeRelative(0, 0, zIndex = 1f)
            content.placeRelative(0, topBar.height)
            if (keyboardMessageLast) message.placeRelative(0, 0, zIndex = 1f)
        }
    }
}

@Composable
private fun Slot(
    id: String,
    order: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .layoutId(id)
            .semantics {
                isTraversalGroup = true
                traversalIndex = order
            }
            .then(modifier)
    ) { content() }
}

/**
 * Where focus waits when the focused message leaves or is replaced (ux Q3, F8): a target before the top bar with no
 * semantics and no action, focusable only while it holds focus. It looks like nothing is focused, Enter and Space do
 * nothing, Tab goes to the screen's first stop and Shift+Tab to its last. With `keyboardMessageLast` the last stop is
 * the message, so Shift+Tab from here goes to the message's control while one shows (Home Today, review 9.3 F3).
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

/**
 * Takes keyboard focus off a control without handing it to another one: to the [MessageLayout]'s park, where it looks
 * like nothing is focused and the next Tab goes to the screen's first stop. Android gives focus back to the first
 * control as soon as it is cleared while a keyboard is in use, so a plain clear is only the fallback outside a
 * [MessageLayout] (ux F8, BRD D181 (6)).
 */
class FocusParking internal constructor(
    private val focusManager: FocusManager,
    private val park: MessageFocusPark?
) {
    fun park() {
        if (park == null || !park.park()) focusManager.clearFocus(force = true)
    }
}

/** The [FocusParking] of the [MessageLayout] around the caller. */
@Composable
fun rememberFocusParking(): FocusParking {
    val focusManager = LocalFocusManager.current
    val park = LocalMessageFocusPark.current
    return remember(focusManager, park) { FocusParking(focusManager, park) }
}

/**
 * For a control whose press removes or disables it, such as a Save that waits or a button that logs and leaves: a
 * press while it has keyboard focus parks focus ([FocusParking]), so Android does not hand focus to a control the
 * user did not choose (BRD D181 (6)). Put [modifier] on the control and call [pressed] first in its click.
 */
class ParkWhenPressed internal constructor(private val parking: FocusParking) {
    private var focused = false

    val modifier: Modifier = Modifier.onFocusChanged { focused = it.isFocused }

    fun pressed() {
        if (focused) parking.park()
    }
}

@Composable
fun rememberParkWhenPressed(): ParkWhenPressed {
    val parking = rememberFocusParking()
    return remember(parking) { ParkWhenPressed(parking) }
}

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
