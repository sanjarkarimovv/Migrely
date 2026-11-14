package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/**
 * Moves what it modifies down by [topOffset], but never so far that it passes the bottom of the safe area ([insets]'s
 * bottom): then it moves down only as far as still fits, and not at all when even that does not fit (BRD D178 (6), ux
 * F5). Where it fits, it is exactly `padding(top = topOffset)`.
 */
internal fun Modifier.fitTopOffset(topOffset: Dp, insets: WindowInsets): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0))
        val offset = fittedOffset(
            wanted = topOffset.roundToPx(),
            height = placeable.height,
            room = if (constraints.hasBoundedHeight) {
                constraints.maxHeight - insets.getBottom(this)
            } else {
                Int.MAX_VALUE
            }
        )
        val height = (offset + placeable.height).coerceIn(
            constraints.minHeight,
            constraints.maxHeight
        )
        layout(placeable.width, height) { placeable.place(0, offset) }
    }

/** How far down something [height] tall goes, at most [wanted], so it still ends within [room]; never negative. */
internal fun fittedOffset(wanted: Int, height: Int, room: Int): Int =
    if (room == Int.MAX_VALUE) wanted else (room - height).coerceIn(0, wanted.coerceAtLeast(0))
