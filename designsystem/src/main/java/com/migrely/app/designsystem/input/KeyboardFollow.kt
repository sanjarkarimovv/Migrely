package com.migrely.app.designsystem.input

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import kotlinx.coroutines.flow.collectLatest

// How far a line may pass the visible area's edge and still count as shown, in px.
private const val EDGE_TOLERANCE = 1f

/**
 * Whether a focused field's line follows the keyboard (BRD D180 (2), (5); ux U6; review 7.1 F1, F2): only while the
 * line is in view. A layout in which the keyboard did not move records what the person sees, so a scroll away, by
 * hand or by the safety card's reveal (D167), ends the following; a layout in which the keyboard moved keeps the
 * record from before the move, as Compose's own resize tracking does. Only a growing keyboard asks for the line: one
 * that shrinks or closes never scrolls the page.
 */
internal class KeyboardFollow {
    private var following = false
    private var keyboardAtLastLayout = -1

    /** The field brought its line into view itself, on focus or an edit. */
    fun onBroughtIntoView() {
        following = true
    }

    /** After a layout: [line], in [field]'s coordinates, is what the person must see; [keyboard] is its height now. */
    fun onPositioned(keyboard: Int, field: LayoutCoordinates?, line: Rect?) {
        if (keyboard == keyboardAtLastLayout && field != null && line != null) {
            following = field.showsWhole(line)
        }
        keyboardAtLastLayout = keyboard
    }

    /** Until cancelled: whenever [keyboard]'s height grows while the line is followed, [bring]s the line into view. */
    suspend fun followKeyboard(keyboard: () -> Int, bring: suspend () -> Unit) {
        var last = -1
        snapshotFlow(keyboard).collectLatest { now ->
            val grew = last in 0 until now
            last = now
            if (grew && following) bring()
        }
    }

    private fun LayoutCoordinates.showsWhole(line: Rect): Boolean {
        if (!isAttached) return false
        val visible = boundsInWindow()
        val top = localToWindow(line.topLeft).y
        val bottom = localToWindow(line.bottomLeft).y
        return top >= visible.top - EDGE_TOLERANCE && bottom <= visible.bottom + EDGE_TOLERANCE
    }
}
