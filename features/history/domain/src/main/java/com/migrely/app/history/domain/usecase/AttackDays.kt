package com.migrely.app.history.domain.usecase

import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Zone offsets differ by at most 26 hours, so two days on each side cover any zone an attack was logged in. */
internal const val ZONE_MARGIN_DAYS = 2L

/**
 * The dates an attack touches in the zone where it started (BRD EC-ATK-06, EC-TIM-02): from its start date to the date
 * just before its end, or to [now] while it is in progress. An attack that ends exactly at midnight does not touch the
 * next day. Home has its own copy until change 5 moves the rule into a shared module (fix design D2).
 */
internal fun HistoryAttackDomainModel.datesTouched(now: Instant): ClosedRange<LocalDate> {
    val first = start.atZone(startZone).toLocalDate()
    val last = (end?.minusNanos(1) ?: now).atZone(startZone).toLocalDate()
    return first..maxOf(first, last)
}

/** The start of [from] minus the margin, in [zone]. */
internal fun windowStart(from: LocalDate, zone: ZoneId): Instant =
    from.minusDays(ZONE_MARGIN_DAYS).atStartOfDay(zone).toInstant()

/** The start of the day after [to] plus the margin, in [zone]. */
internal fun windowEnd(to: LocalDate, zone: ZoneId): Instant =
    to.plusDays(1 + ZONE_MARGIN_DAYS).atStartOfDay(zone).toInstant()
