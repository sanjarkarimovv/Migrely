package com.migrely.app.designsystem.input

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.constrainHeight

/**
 * Room below the content of a scrolling page whose visible part ends at the keyboard, the page padding that part by
 * [viewportInsets] (BRD D180 (2)). As the keyboard closes the room grows by what the visible part gains, so the page
 * keeps its scroll offset (#238): a page pulled down instead is reported as a scroll, and TalkBack then moves its focus
 * off the field to the first item it sees. The room shrinks to what is in view as the page is scrolled back and as the
 * keyboard opens again, so it never moves the page itself. Put [modifier] right after the page's `verticalScroll`.
 */
class KeyboardRoom internal constructor(private val viewportInsets: WindowInsets) {
    // The largest padding of the visible part the room still makes up for, in px.
    private var peak by mutableIntStateOf(0)

    val modifier: Modifier = Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val room = height(viewportInsets.getBottom(this))
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

/** The [KeyboardRoom] of the page scrolled by [scrollState]. */
@Composable
fun rememberKeyboardRoom(scrollState: ScrollState, viewportInsets: WindowInsets): KeyboardRoom {
    val density = LocalDensity.current
    val room = remember(viewportInsets) { KeyboardRoom(viewportInsets) }
    LaunchedEffect(room, scrollState, density) {
        // Read again when collected: a layout between the two would make the values stale.
        val bottom = { viewportInsets.getBottom(density) }
        val below = { scrollState.maxValue - scrollState.value }
        snapshotFlow { bottom() to below() }.collect { room.onChange(bottom(), below()) }
    }
    return room
}
