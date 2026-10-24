package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * When the daily reminder runs next (BRD D66, EC-REM-05), always worked out from the device's zone at the moment, so a
 * trip or a change of clock moves it to the new local time. A time that a daylight-saving jump skips moves forward.
 */
internal class DailyReminderTime {
    /** The first [time] strictly after now: today if it is still ahead, otherwise tomorrow. */
    fun next(now: CurrentTimeDomainModel, time: LocalTime): Instant {
        val today = now.instant.atZone(now.zone).toLocalDate()
        val todayAt = at(today, time, now.zone)
        return if (todayAt.isAfter(now.instant)) todayAt else at(today.plusDays(1), time, now.zone)
    }

    /**
     * The run after the one for [date]: the next day's [time], so a run that came a little early never repeats its own
     * day; or, when that has passed too, the first [time] after now, so a run that came very late loses no day.
     */
    fun after(date: LocalDate, now: CurrentTimeDomainModel, time: LocalTime): Instant {
        val nextDay = at(date.plusDays(1), time, now.zone)
        return if (nextDay.isAfter(now.instant)) nextDay else next(now, time)
    }

    /** The date a run at [at] asks about, in the device's zone. */
    fun dateOf(at: Instant, zone: ZoneId): LocalDate = at.atZone(zone).toLocalDate()

    private fun at(date: LocalDate, time: LocalTime, zone: ZoneId): Instant =
        ZonedDateTime.of(date, time, zone).toInstant()
}
