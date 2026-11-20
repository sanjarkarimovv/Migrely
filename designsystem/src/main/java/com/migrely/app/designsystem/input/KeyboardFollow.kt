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
 * record from before the move, as Compose's own resize tracking does. So does a layout at a height the follow has not
 * acted on yet: with an accessibility service on, Compose lays the screen out again between the keyboard's frame and
 * the follow, and that layout, with the line under the keyboard, ended the following (#259). Only a growing keyboard
 * asks for the line: one that shrinks or closes never scrolls the page.
 */
internal class KeyboardFollow {
    private var following = false
    private var keyboardAtLastLayout = -1

    // The keyboard height the follow's collector last acted on.
    private var keyboardFollowed = -1

    // The field's own bring of its line is running: the line is on its way into view.
    private var bringing = false

    /** Whether the line follows the keyboard now. */
    val isFollowing: Boolean
        get() = following

    /**
     * The field starts bringing its line into view itself, on focus or an edit: it follows the keyboard from now, not
     * only once the bring ends, so a keyboard that comes while the page still moves keeps the line (#259 review F1).
     * Layouts during the bring record nothing; [onBringEnded] ends it, ended or cancelled.
     */
    fun onBringing() {
        bringing = true
        following = true
    }

    /**
     * The field's bring ended, done or cancelled (a drag by the person cancels it): the next layout in which the
     * keyboard holds still records what the person sees again (BRD D180 (5)).
     */
    fun onBringEnded() {
        bringing = false
    }

    /**
     * The line was brought into view, by the field itself on focus or an edit, or by its typing space when the keyboard
     * was about to hide the field (#259, decision (a')): it follows the keyboard until a settled layout says otherwise.
     */
    fun onBroughtIntoView() {
        following = true
    }

    /** After a layout: [line], in [field]'s coordinates, is what the person must see; [keyboard] is its height now. */
    fun onPositioned(keyboard: Int, field: LayoutCoordinates?, line: Rect?) {
        // The keyboard held still since the last layout, and the follow has already acted on this height.
        val settled = keyboard == keyboardAtLastLayout && keyboard == keyboardFollowed
        if (settled && !bringing && field != null && line != null) {
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
            keyboardFollowed = now
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
