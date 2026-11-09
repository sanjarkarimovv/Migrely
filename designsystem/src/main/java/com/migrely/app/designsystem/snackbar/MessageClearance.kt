package com.migrely.app.designsystem.snackbar

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.offset

/**
 * Keeps what it modifies below a message: it moves down by the space from its own top to [messageBottom], the
 * message's bottom in root coordinates as [MigrelySnackbarHost]'s `onMessageBottomChanged` reports it, and by nothing
 * when that is 0 dp or above it (BRD D52, D165 (1)). Put it before a `verticalScroll`, so the visible area starts below
 * the message, scrolled or not. [messageBottom] is read while measuring, so the content follows the host's own timing
 * frame by frame without recomposing, and with reduce motion it moves at once, like the host.
 */
fun Modifier.clearOfMessage(messageBottom: () -> Dp): Modifier =
    this then ClearOfMessageElement(messageBottom)

/** The space kept above content whose top is [contentTop]: down to [messageBottom], never negative. */
internal fun messageClearance(messageBottom: Dp, contentTop: Dp): Dp =
    if (contentTop.isUnspecified) 0.dp else (messageBottom - contentTop).coerceAtLeast(0.dp)

private data class ClearOfMessageElement(val messageBottom: () -> Dp) :
    ModifierNodeElement<ClearOfMessageNode>() {
    override fun create() = ClearOfMessageNode(messageBottom)

    override fun update(node: ClearOfMessageNode) {
        node.messageBottom = messageBottom
        node.invalidateMeasurement()
    }
}

private class ClearOfMessageNode(var messageBottom: () -> Dp) :
    Modifier.Node(),
    LayoutModifierNode,
    GlobalPositionAwareModifierNode {
    // Where the parent places this node, which does not depend on the space it keeps, so the two never feed each other.
    private var contentTop = Dp.Unspecified

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val top = messageClearance(messageBottom(), contentTop).roundToPx()
        val placeable = measurable.measure(constraints.offset(vertical = -top))
        return layout(placeable.width, constraints.constrainHeight(placeable.height + top)) {
            placeable.place(0, top)
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val top = with(requireDensity()) { coordinates.positionInRoot().y.toDp() }
        if (top != contentTop) {
            contentTop = top
            invalidateMeasurement()
        }
    }
}
