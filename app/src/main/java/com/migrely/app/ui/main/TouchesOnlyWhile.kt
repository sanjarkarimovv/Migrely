package com.migrely.app.ui.main

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize

/**
 * Consumes the pointer input of this window on the content while [allowed] is false, read at the
 * moment of each event. A finger already down when it turns false therefore cancels instead of
 * clicking. Semantics are not touched, so screen readers and switch access keep working, and a
 * dialog or popup is a window of its own, which this does not reach.
 *
 * It also consumes what [SecondTapRule] refuses, in the same pass, so that rule has the same hit
 * testing and the same untouched semantics.
 *
 * The node must take part only in touches that hit something inside the content. A node that a
 * touch hits by itself ends the search there, and during a pop, where the leaving screen is on top,
 * the screen fading in below it would stop taking taps. Compose counts a node that a touch misses
 * only when a child is hit and the node intercepts out-of-bounds child events. So the node's own
 * bounds are moved one width aside, and the content is moved back by the same amount. This rests
 * on Compose's hit testing (`NodeCoordinator.speculativeHit`, Compose 1.9.3).
 * `LeavingScreenInputTest` fails if an upgrade changes it.
 */
internal fun Modifier.touchesOnlyWhile(allowed: () -> Boolean, entry: SecondTapEntry): Modifier =
    this
        .shiftedByWidths(-1)
        .then(TouchesOnlyWhileElement(allowed, entry))
        .shiftedByWidths(1)

/** Which entry this is, for [SecondTapRule], which is asked in the same pass. */
internal data class SecondTapEntry(
    val id: String,
    val rule: SecondTapRule,
    val limits: SecondTapLimits
)

private fun Modifier.shiftedByWidths(widths: Int): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(widths * placeable.width, 0)
    }
}

private data class TouchesOnlyWhileElement(val allowed: () -> Boolean, val entry: SecondTapEntry) :
    ModifierNodeElement<TouchesOnlyWhileNode>() {
    override fun create() = TouchesOnlyWhileNode(allowed, entry)

    override fun update(node: TouchesOnlyWhileNode) {
        node.allowed = allowed
        node.entry = entry
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "touchesOnlyWhile"
    }
}

private class TouchesOnlyWhileNode(var allowed: () -> Boolean, var entry: SecondTapEntry) :
    Modifier.Node(),
    PointerInputModifierNode {
    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        if (pass != PointerEventPass.Initial) return
        val current = allowed()
        pointerEvent.changes.forEach { change ->
            if (shouldConsume(change, current)) change.consume()
        }
    }

    private fun shouldConsume(change: PointerInputChange, current: Boolean): Boolean {
        val pointerId = change.id.value
        val ignored = when {
            change.changedToDownIgnoreConsumed() ->
                current && entry.rule.press(entry.id, pointerId, change.touchPoint(), entry.limits)

            else -> entry.rule.isIgnored(pointerId)
        }
        if (change.changedToUpIgnoreConsumed()) {
            entry.rule.lift(entry.id, pointerId, change.touchPoint())
        }
        return !current || ignored
    }

    private fun PointerInputChange.touchPoint() = TouchPoint(uptimeMillis, position.x, position.y)

    override fun onCancelPointerInput() = entry.rule.forgetPresses()

    override fun interceptOutOfBoundsChildEvents() = true
}
