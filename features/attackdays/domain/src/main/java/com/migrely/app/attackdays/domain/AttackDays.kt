package com.migrely.app.attackdays.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Which calendar dates an attack belongs to, the one rule for home, History and the month's numbers (BRD EC-ATK-06,
 * EC-TIM-02, D36, D57).
 */
object AttackDays {
    /** Zone offsets differ by at most 26 hours, so two days on each side cover any zone an attack was logged in. */
    const val ZONE_MARGIN_DAYS = 2L

    /**
     * The dates an attack touches in [startZone], where it started: from its start date to the date just before [end],
     * or to [now] while it is in progress. An attack that ends exactly at midnight does not touch the next day.
     */
    fun datesTouched(
        start: Instant,
        startZone: ZoneId,
        end: Instant?,
        now: Instant
    ): ClosedRange<LocalDate> {
        val first = start.atZone(startZone).toLocalDate()
        val last = (end?.minusNanos(1) ?: now).atZone(startZone).toLocalDate()
        return first..maxOf(first, last)
    }

    /**
     * The start of [from] minus the margin, in [zone]: reading from here finds every attack, or intake, logged in any
     * zone that may fall on [from].
     */
    fun windowStart(from: LocalDate, zone: ZoneId): Instant =
        from.minusDays(ZONE_MARGIN_DAYS).atStartOfDay(zone).toInstant()

    /** The start of the day after [to] plus the margin, in [zone]. */
    fun windowEnd(to: LocalDate, zone: ZoneId): Instant =
        to.plusDays(1 + ZONE_MARGIN_DAYS).atStartOfDay(zone).toInstant()
}
