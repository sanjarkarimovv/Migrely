package com.migrely.app.ui.main

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
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
 * The node must take part only in touches that hit something inside the content. A node that a
 * touch hits by itself ends the search there, and during a pop, where the leaving screen is on top,
 * the screen fading in below it would stop taking taps. Compose counts a node that a touch misses
 * only when a child is hit and the node intercepts out-of-bounds child events. So the node's own
 * bounds are moved one width aside, and the content is moved back by the same amount. This rests
 * on Compose's hit testing (`NodeCoordinator.speculativeHit`, Compose 1.9.3).
 * `LeavingScreenInputTest` fails if an upgrade changes it.
 */
internal fun Modifier.touchesOnlyWhile(allowed: () -> Boolean): Modifier = this
    .shiftedByWidths(-1)
    .then(TouchesOnlyWhileElement(allowed))
    .shiftedByWidths(1)

private fun Modifier.shiftedByWidths(widths: Int): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(widths * placeable.width, 0)
    }
}

private data class TouchesOnlyWhileElement(val allowed: () -> Boolean) :
    ModifierNodeElement<TouchesOnlyWhileNode>() {
    override fun create() = TouchesOnlyWhileNode(allowed)

    override fun update(node: TouchesOnlyWhileNode) {
        node.allowed = allowed
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "touchesOnlyWhile"
    }
}

private class TouchesOnlyWhileNode(var allowed: () -> Boolean) :
    Modifier.Node(),
    PointerInputModifierNode {
    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        if (pass == PointerEventPass.Initial && !allowed()) {
            pointerEvent.changes.forEach { it.consume() }
        }
    }

    override fun onCancelPointerInput() = Unit

    override fun interceptOutOfBoundsChildEvents() = true
}
