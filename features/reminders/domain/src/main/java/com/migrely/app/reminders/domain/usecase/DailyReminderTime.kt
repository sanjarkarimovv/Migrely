package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * When the daily reminder runs next (BRD D66, EC-REM-05), always worked out from the device's zone at the moment, so a
 * trip or a change of clock moves it to the new local time. A time that a daylight-saving jump skips moves forward.
 */
internal class DailyReminderTime {
    /** The first [time] strictly after now: today if it is still ahead, otherwise tomorrow. */
    fun next(now: CurrentTimeDomainModel, time: LocalTime): Instant {
        val today = now.instant.atZone(now.zone).toLocalDate()
        val todayAt = at(today, time, now)
        return if (todayAt.isAfter(now.instant)) todayAt else at(today.plusDays(1), time, now)
    }

    /** [time] on the day after now's date, so a run that came a little early never repeats the same day. */
    fun tomorrow(now: CurrentTimeDomainModel, time: LocalTime): Instant =
        at(now.instant.atZone(now.zone).toLocalDate().plusDays(1), time, now)

    private fun at(date: LocalDate, time: LocalTime, now: CurrentTimeDomainModel): Instant =
        ZonedDateTime.of(date, time, now.zone).toInstant()
}
