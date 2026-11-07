package com.migrely.app.designsystem.dialog

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Every dialog ignores touches on its content for this long from when it appears, so the second tap
 * of the double tap that opened it cannot run its button (issue #157, BRD D160). One number for
 * all. It is where QA's measures stopped, not where writes were seen to stop.
 */
const val DIALOG_WAIT_MILLIS = 500L

/**
 * Whether a press lands in a dialog's wait, judged by the press's own event time, so a stalled main
 * thread cannot let one through. A press that does stays ignored until its pointer lifts, so a
 * finger held down does not get past the wait.
 */
internal class DialogWaitRule {
    private val ignoredPointers = mutableSetOf<Long>()

    /** True when this press is to be ignored. */
    fun press(pointerId: Long, pressedAtMillis: Long, appearedAtMillis: Long): Boolean {
        val ignored = pressedAtMillis - appearedAtMillis < DIALOG_WAIT_MILLIS
        if (ignored) ignoredPointers += pointerId else ignoredPointers -= pointerId
        return ignored
    }

    fun isIgnored(pointerId: Long): Boolean = pointerId in ignoredPointers

    fun lift(pointerId: Long) {
        ignoredPointers -= pointerId
    }
}

/**
 * The clock touch events use. In a Compose test they use the test's clock, so this module's tests
 * provide it. A test in another module cannot, and its touches on a dialog never answer at any
 * delay, so it presses the dialog's controls through their click action, which the wait leaves
 * alone.
 */
internal val LocalDialogClock = staticCompositionLocalOf<() -> Long> { SystemClock::uptimeMillis }

/**
 * A modifier and not a layer, because Material's `AlertDialog` has no slot for one: what it offers
 * is a `modifier`, on the box around the card. It adds no semantics, so a screen reader's or switch
 * access's click action and a keyboard work at once, and it draws nothing.
 */
private fun Modifier.dialogWait(): Modifier = this.then(DialogWaitElement)

private object DialogWaitElement : ModifierNodeElement<DialogWaitNode>() {
    override fun create() = DialogWaitNode()

    override fun update(node: DialogWaitNode) = Unit

    override fun hashCode() = "dialogWait".hashCode()

    override fun equals(other: Any?) = other === this

    override fun InspectorInfo.inspectableProperties() {
        name = "dialogWait"
    }
}

private class DialogWaitNode :
    Modifier.Node(),
    PointerInputModifierNode,
    CompositionLocalConsumerModifierNode {
    private val rule = DialogWaitRule()
    private var appearedAtMillis = 0L

    override fun onAttach() {
        appearedAtMillis = currentValueOf(LocalDialogClock)()
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        if (pass != PointerEventPass.Initial) return
        val lifts = currentValueOf(LocalDialogLiftRule)
        pointerEvent.changes.forEach { change ->
            val pointerId = change.id.value
            val ignored = if (change.changedToDownIgnoreConsumed()) {
                // Both are told, and each keeps its own pointers. An older dialog is beneath a lift.
                val inTheWait = rule.press(pointerId, change.uptimeMillis, appearedAtMillis)
                val afterALift = lifts?.let { pressAfterALift(it, change) } == true
                inTheWait || afterALift
            } else {
                rule.isIgnored(pointerId) || lifts?.isIgnored(pointerId) == true
            }
            if (change.changedToUpIgnoreConsumed()) {
                // A press the wait ignored ran nothing, so it does not count as a lift on the card.
                val letThrough = !rule.isIgnored(pointerId) || lifts?.isIgnored(pointerId) == true
                rule.lift(pointerId)
                if (letThrough) lifts?.lift(this, pointerId, change.uptimeMillis, change.onScreen())
            }
            if (ignored) change.consume()
        }
    }

    private fun pressAfterALift(lifts: DialogLiftRule, change: PointerInputChange) = lifts.press(
        windowKey = this,
        pointerId = change.id.value,
        atMillis = change.uptimeMillis,
        screenPosition = change.onScreen(),
        timeoutMillis = currentValueOf(LocalViewConfiguration).doubleTapTimeoutMillis,
        distance = ViewConfiguration.get(currentValueOf(LocalContext)).scaledDoubleTapSlop.toFloat()
    )

    private fun PointerInputChange.onScreen() = requireLayoutCoordinates().localToScreen(position)

    override fun onCancelPointerInput() = Unit
}

/**
 * During the wait a tap outside the card does not close the dialog: a dialog that opens and shuts
 * looks like a control that did nothing. Back is not touched. A press that began outside in the
 * wait does not close it when it lifts later, because the dialog only closes on a press it saw go
 * down while this was on.
 */
@Composable
private fun rememberPropertiesInTheWait(properties: DialogProperties): DialogProperties {
    var waiting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(DIALOG_WAIT_MILLIS)
        waiting = false
    }
    return if (waiting) {
        DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = false,
            securePolicy = properties.securePolicy,
            usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
            decorFitsSystemWindows = properties.decorFitsSystemWindows
        )
    } else {
        properties
    }
}

/** Material's `AlertDialog`, with the wait. */
@Composable
fun MigrelyAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = Modifier.dialogWait(),
        dismissButton = dismissButton,
        title = title,
        text = text,
        properties = rememberPropertiesInTheWait(DialogProperties())
    )
}

/** Material's `DatePickerDialog`, with the wait. */
@Composable
fun MigrelyDatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = Modifier.dialogWait(),
        dismissButton = dismissButton,
        properties = rememberPropertiesInTheWait(DialogProperties(usePlatformDefaultWidth = false)),
        content = content
    )
}

/**
 * Compose's `Dialog`, with the wait. [fullScreen] has no outside to hold off, so its properties
 * stay as they are. The box hands on the constraints the dialog gave.
 */
@Composable
fun MigrelyDialog(
    onDismissRequest: () -> Unit,
    fullScreen: Boolean = false,
    content: @Composable () -> Unit
) {
    val properties = if (fullScreen) {
        DialogProperties(usePlatformDefaultWidth = false)
    } else {
        rememberPropertiesInTheWait(DialogProperties())
    }
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Box(Modifier.dialogWait(), propagateMinConstraints = true) { content() }
    }
}
