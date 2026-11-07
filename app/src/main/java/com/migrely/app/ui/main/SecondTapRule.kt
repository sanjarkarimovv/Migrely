package com.migrely.app.ui.main

import kotlin.math.hypot

/** A touch at a moment and a place, from the touch event itself, in the node's own coordinates. */
internal data class TouchPoint(val uptimeMillis: Long, val x: Float, val y: Float)

/** Android's own definition of a double tap on this device. */
internal data class SecondTapLimits(val timeoutMillis: Long, val distance: Float)

/**
 * Whether a press is the second tap of a double tap, landing on a screen the first tap opened (issue
 * #156, BRD D159). It is if it comes under the timeout after the last lift, within the distance of
 * that lift, on an entry no touch has lifted on yet. Which entries were touched is kept on the entry
 * itself, so that it survives a recreation and goes with a pop; this class is told it. So a return to
 * a screen the user tapped to leave is never covered. The last lift and the ignored pointers are not
 * saved: touch event times mean nothing later, and this window does not outlast a recreation.
 *
 * An ignored press stays ignored until its pointer lifts. Its lift moves the last lift, so a quick
 * burst stays ignored until a pause, and does not count as a touch on the entry, so the screen stays
 * new. A pointer cancelled by the system never lifts and its id stays here: touch ids are not reused
 * and a later press of the same id decides afresh, so that is only a few stale numbers.
 */
internal class SecondTapRule {
    private var lastLift: TouchPoint? = null
    private val ignoredPointers = mutableSetOf<Long>()

    /** True when this press is to be ignored. */
    fun press(
        pointerId: Long,
        at: TouchPoint,
        limits: SecondTapLimits,
        entryTouched: Boolean
    ): Boolean {
        val lift = lastLift
        val ignored = lift != null &&
            !entryTouched &&
            at.uptimeMillis - lift.uptimeMillis < limits.timeoutMillis &&
            hypot(at.x - lift.x, at.y - lift.y) < limits.distance
        if (ignored) ignoredPointers += pointerId else ignoredPointers -= pointerId
        return ignored
    }

    /** True when this lift counts as a touch on the entry: it was not an ignored press. */
    fun lift(pointerId: Long, at: TouchPoint): Boolean {
        lastLift = at
        return !ignoredPointers.remove(pointerId)
    }

    fun isIgnored(pointerId: Long): Boolean = pointerId in ignoredPointers
}
