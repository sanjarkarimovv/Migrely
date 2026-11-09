package com.migrely.app.reminders.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val REMINDER_TIME: LocalTime = LocalTime.of(10, 0)
private const val DAYS_BEFORE = 2L

/**
 * When the visit reminder runs (BRD D168, design D3): 10:00 two days before the visit, by the phone's zone at the moment,
 * so a trip or a clock change moves it to the new local time while the visit's date stays. A time that a
 * daylight-saving jump skips moves forward; 10:00 is in no gap in any current zone.
 */
internal class VisitReminderTime {
    /**
     * 10:00 on [postponedTo] when an attack moved the reminder there, which counts only while it lies between two days
     * before the visit and the visit day, so a new date ignores it; otherwise 10:00 two days before (EC-REM-12).
     */
    fun remindAt(visit: LocalDate, postponedTo: LocalDate?, zone: ZoneId): Instant {
        val first = visit.minusDays(DAYS_BEFORE)
        val day = postponedTo?.takeIf { !it.isBefore(first) && !it.isAfter(visit) } ?: first
        return ZonedDateTime.of(day, REMINDER_TIME, zone).toInstant()
    }

    /** The first moment after the visit day, when its notification and card go (EC-REM-15). */
    fun dayEnd(visit: LocalDate, zone: ZoneId): Instant =
        visit.plusDays(1).atStartOfDay(zone).toInstant()

    /** Whether the reminder's own time has come for [visit] at [now]; set then, it is too close to remind (EC-REM-08). */
    fun tooClose(visit: LocalDate, now: Instant, zone: ZoneId): Boolean =
        !now.isBefore(remindAt(visit, postponedTo = null, zone))
}
