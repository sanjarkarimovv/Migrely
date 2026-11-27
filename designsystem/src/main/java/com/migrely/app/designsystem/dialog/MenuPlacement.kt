package com.migrely.app.designsystem.dialog

import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.max
import kotlin.math.min

/**
 * The room a popup menu has above and below its anchor, in the window's pixels: between the anchor and the window's
 * safe edges, less [margin] at each edge (BRD EC-UX-91). Negative when the anchor reaches past that line.
 */
internal data class MenuRoom(val above: Int, val below: Int) {
    /** The most the menu may be tall: all of the larger side, its content scrolling. */
    val maximumHeight: Int get() = max(max(above, below), 0)

    /** The menu's top for its [height]: below when it fits, else above, else on the larger side. Never on [anchor]. */
    fun top(anchor: IntRect, height: Int): Int = when {
        height <= below -> anchor.bottom
        height <= above -> anchor.top - height
        below >= above -> anchor.bottom
        else -> anchor.top - height
    }

    companion object {
        fun of(anchor: IntRect, safeTop: Int, safeBottom: Int, margin: Int) = MenuRoom(
            above = anchor.top - safeTop - margin,
            below = safeBottom - margin - anchor.bottom
        )
    }
}

/**
 * Places the menu by [MenuRoom] vertically and as Material's `DropdownMenu` does horizontally: at the anchor's start
 * edge, else its end edge, else the nearer window edge. Material's own vertical rule falls back to covering the anchor
 * when the menu fits neither side with 48 dp (#179). [safeTop] and [safeBottom] are in the anchor's window, as the
 * anchor's bounds are. [onPlaced] hears each placement, which sizes the menu and sets where it grows from.
 */
internal class MenuPositionProvider(
    private val safeTop: Int,
    private val safeBottom: Int,
    private val margin: Int,
    private val onPlaced: (anchor: IntRect, menu: IntRect) -> Unit
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val room = MenuRoom.of(anchorBounds, safeTop, safeBottom, margin)
        val position = IntOffset(
            x = left(anchorBounds, windowSize.width, popupContentSize.width, layoutDirection),
            y = room.top(anchorBounds, popupContentSize.height)
        )
        onPlaced(anchorBounds, IntRect(position, popupContentSize))
        return position
    }

    private fun left(
        anchor: IntRect,
        windowWidth: Int,
        width: Int,
        direction: LayoutDirection
    ): Int {
        val ltr = direction == LayoutDirection.Ltr
        val atStart = if (ltr) anchor.left else anchor.right - width
        val atEnd = if (ltr) anchor.right - width else anchor.left
        val atWindowEdge = if (anchor.center.x < windowWidth / 2) 0 else windowWidth - width
        return listOf(atStart, atEnd).firstOrNull { it >= 0 && it + width <= windowWidth }
            ?: atWindowEdge
    }
}

/** Where the menu grows from as it opens: the part of its edge nearest the anchor, as Material's menu does. */
internal fun menuTransformOrigin(anchor: IntRect, menu: IntRect): TransformOrigin {
    val pivotX = when {
        menu.left >= anchor.right -> 0f
        menu.right <= anchor.left -> 1f
        menu.width == 0 -> 0f
        else -> {
            val sharedCentre = (max(anchor.left, menu.left) + min(anchor.right, menu.right)) / 2
            (sharedCentre - menu.left).toFloat() / menu.width
        }
    }
    val pivotY = if (menu.top >= anchor.bottom) 0f else 1f
    return TransformOrigin(pivotX, pivotY)
}
