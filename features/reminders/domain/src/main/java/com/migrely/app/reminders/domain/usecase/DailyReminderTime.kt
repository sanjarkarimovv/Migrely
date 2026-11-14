package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * When the daily reminder runs next (BRD D66, EC-REM-05), always worked out from the device's zone at the moment, so a
 * trip or a change of clock moves it to the new local time. A time that a daylight-saving jump skips moves forward. A
 * start or a clock change never skips a day whose reminder has not shown (D176).
 */
internal class DailyReminderTime {
    /** The first [time] strictly after now: today if it is still ahead, otherwise tomorrow. */
    fun next(now: CurrentTimeDomainModel, time: LocalTime): Instant {
        val today = now.instant.atZone(now.zone).toLocalDate()
        val todayAt = at(today, time, now.zone)
        return if (todayAt.isAfter(now.instant)) todayAt else at(today.plusDays(1), time, now.zone)
    }

    /**
     * Where a start or a clock change aims the daily reminder (BRD D176, EC-REM-25, EC-REM-26): today's [time] while it
     * is still ahead; once it has passed, now, for today, unless today's reminder showed on [handledOn]; otherwise
     * tomorrow's [time].
     */
    fun due(now: CurrentTimeDomainModel, time: LocalTime, handledOn: LocalDate?): Instant {
        val today = now.instant.atZone(now.zone).toLocalDate()
        val passed = !at(today, time, now.zone).isAfter(now.instant)
        return if (passed && handledOn != today) now.instant else next(now, time)
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
