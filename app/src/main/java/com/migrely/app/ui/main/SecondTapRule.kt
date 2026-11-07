package com.migrely.app.ui.main

import kotlin.math.hypot

/** A touch at a moment and a place, from the touch event itself, in the node's own coordinates. */
internal data class TouchPoint(val uptimeMillis: Long, val x: Float, val y: Float)

/** Android's own definition of a double tap on this device. */
internal data class SecondTapLimits(val timeoutMillis: Long, val distance: Float)

/**
 * Whether a press is the second tap of a double tap, landing on a screen the first tap opened (issue
 * #156, BRD D159). It is if it comes under the timeout after the last lift, within the distance of
 * that lift, on an entry no touch has lifted on yet. So a return to a screen the user tapped to
 * leave is never covered. One instance serves the nav host, because a screen's node does not outlive
 * the screen leaving the composition and the entry's mark must.
 *
 * An ignored press stays ignored until its pointer lifts. Its lift moves the last lift, so a quick
 * burst stays ignored until a pause, and does not mark the entry, so the screen stays new.
 */
internal class SecondTapRule {
    private var lastLift: TouchPoint? = null
    private val touchedEntries = mutableSetOf<String>()
    private val ignoredPointers = mutableSetOf<Long>()

    /** True when this press is to be ignored. */
    fun press(entryId: String, pointerId: Long, at: TouchPoint, limits: SecondTapLimits): Boolean {
        val lift = lastLift
        val ignored = lift != null &&
            entryId !in touchedEntries &&
            at.uptimeMillis - lift.uptimeMillis < limits.timeoutMillis &&
            hypot(at.x - lift.x, at.y - lift.y) < limits.distance
        if (ignored) ignoredPointers += pointerId
        return ignored
    }

    fun lift(entryId: String, pointerId: Long, at: TouchPoint) {
        lastLift = at
        if (!ignoredPointers.remove(pointerId)) touchedEntries += entryId
    }

    fun isIgnored(pointerId: Long): Boolean = pointerId in ignoredPointers

    /** The system took the touches back: nothing will lift. */
    fun forgetPresses() = ignoredPointers.clear()
}
