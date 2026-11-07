package com.migrely.app.designsystem.dialog

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset

/**
 * Whether a press is the second tap of a double tap whose first lifted on a dialog's card (issue
 * #157, #159, BRD D161). The first tap can close the dialog, and the second then lands on the
 * window beneath, which is not this dialog's wait, and D159 cannot hear a lift in another window.
 * So a press in any other window, the main screen or an older dialog, is ignored until it lifts, if
 * it comes under [DIALOG_WAIT_MILLIS] after the lift and within the distance of it. That is longer
 * than D159's limit because a false write there costs more than one lost gesture. A dialog that
 * appeared after the lift was not beneath it, so its own wait alone decides. Times are the touch
 * events' own, and places are on the screen, because two windows have different origins.
 *
 * A touch id means something only in its own window, so ignored pointers are kept per window. Only
 * a dialog's lift is a lift here. The lift of an ignored press moves the time and place and stays
 * that dialog's, and the next press is judged by [burstTimeoutMillis], the platform's double tap,
 * so retrying a lost tap at an ordinary pace answers. Nothing is saved: this does not outlast a
 * recreation.
 */
class DialogLiftRule(private val burstTimeoutMillis: Long) {
    private var lastLift: Lift? = null
    private val ignoredPointers = mutableSetOf<Pair<Any?, Long>>()

    /**
     * True when this press is to be ignored. [windowKey] is null for the main window, which is
     * there before any lift. [windowAppearedAtMillis] is when a dialog's window appeared.
     */
    @Suppress("LongParameterList")
    fun press(
        windowKey: Any?,
        pointerId: Long,
        atMillis: Long,
        screenPosition: Offset,
        distance: Float,
        windowAppearedAtMillis: Long = Long.MIN_VALUE
    ): Boolean {
        val lift = lastLift
        val ignored = lift != null &&
            lift.windowKey != windowKey &&
            lift.atMillis >= windowAppearedAtMillis &&
            atMillis - lift.atMillis < lift.limitMillis &&
            (screenPosition - lift.screenPosition).getDistance() < distance
        val pointer = windowKey to pointerId
        if (ignored) ignoredPointers += pointer else ignoredPointers -= pointer
        return ignored
    }

    /** A lift that was an ignored press, in any window, or one on a dialog's card ([windowKey]). */
    fun lift(windowKey: Any?, pointerId: Long, atMillis: Long, screenPosition: Offset) {
        val wasIgnored = ignoredPointers.remove(windowKey to pointerId)
        val last = lastLift
        if (wasIgnored && last != null) {
            lastLift = Lift(last.windowKey, atMillis, screenPosition, burstTimeoutMillis)
        } else if (windowKey != null) {
            lastLift = Lift(windowKey, atMillis, screenPosition, DIALOG_WAIT_MILLIS)
        }
    }

    fun isIgnored(windowKey: Any?, pointerId: Long): Boolean =
        (windowKey to pointerId) in ignoredPointers

    private data class Lift(
        val windowKey: Any,
        val atMillis: Long,
        val screenPosition: Offset,
        val limitMillis: Long
    )
}

/** The app's nav host provides the one instance. With none, in a preview, nothing is ignored. */
val LocalDialogLiftRule = staticCompositionLocalOf<DialogLiftRule?> { null }
