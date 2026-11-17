package com.migrely.app.designsystem.input

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.constrainHeight

/**
 * Room below the content of a scrolling page whose visible part ends at the keyboard, the page padding that part's
 * bottom by [viewportBottom] px (BRD D180 (2)). As the keyboard closes the room grows by what the visible part gains,
 * so the page keeps its scroll offset (BRD D189, #238): a page pulled down instead is reported as a scroll, and
 * TalkBack then moves its focus off the field to the first item it sees. The room shrinks to what is in view as the
 * page is scrolled back and as the keyboard opens again, so it never moves the page itself. It draws nothing, so the
 * page's own background shows, and it adds no semantics node. It is not saved: a recreated screen has none.
 * Put [modifier] right after the page's `verticalScroll`, never as a child, which a `spacedBy` column would space.
 */
class KeyboardRoom internal constructor(private val viewportBottom: State<Density.() -> Int>) {
    // The largest padding of the visible part the room still makes up for, in px.
    private var peak by mutableIntStateOf(0)

    val modifier: Modifier = Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val room = height(viewportBottom.value(this))
        layout(placeable.width, constraints.constrainHeight(placeable.height + room)) {
            placeable.place(0, 0)
        }
    }

    private fun height(bottom: Int) = (peak - bottom).coerceAtLeast(0)

    /**
     * The visible part is padded by [bottom] px and [below] px of the page lie under it: the room keeps what is in
     * view, and so never takes more than [below] from the page's length.
     */
    internal fun onChange(bottom: Int, below: Int) {
        val room = height(bottom)
        peak = maxOf(bottom, peak - minOf(room, below.coerceAtLeast(0)))
    }
}

/** The [KeyboardRoom] of the page scrolled by [scrollState], whose visible part is padded by [viewportInsets]. */
@Composable
fun rememberKeyboardRoom(scrollState: ScrollState, viewportInsets: WindowInsets): KeyboardRoom =
    rememberKeyboardRoom(scrollState) { viewportInsets.getBottom(this) }

/**
 * The [KeyboardRoom] of the page scrolled by [scrollState], whose visible part ends [viewportBottom] px above the
 * bottom of the area it is laid out in, for a page whose visible part a layout sets rather than an inset (Delete all
 * data's row, BRD D180 (8)). [viewportBottom] is read while the page is laid out: whatever moves with the keyboard
 * must be read there too (insets), as the page's own padding reads it. A value that moves with the keyboard but is
 * set in composition can lag the layout by a pass, and a room that grows before the visible part does is counted as
 * page below the view and taken away, so the page is pulled after all. Values that hold still while the keyboard
 * moves (a row's own size, a choice made once per keyboard) may come from composition.
 */
@Composable
fun rememberKeyboardRoom(
    scrollState: ScrollState,
    viewportBottom: Density.() -> Int
): KeyboardRoom {
    val density = LocalDensity.current
    val bottom = rememberUpdatedState(viewportBottom)
    val room = remember { KeyboardRoom(bottom) }
    LaunchedEffect(room, scrollState, density) {
        // Read again when collected: a layout between the two would make the values stale.
        val bottomPx = { bottom.value(density) }
        val below = { scrollState.maxValue - scrollState.value }
        snapshotFlow { bottomPx() to below() }.collect { room.onChange(bottomPx(), below()) }
    }
    return room
}
