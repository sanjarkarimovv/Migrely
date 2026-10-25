package com.migrely.app.patterns.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * When Insights have enough to show patterns, the one rule for Insights and for the paywall that
 * promises them after the 5th migraine (BRD EC-INS-01, D88, D110): at least 5 migraines started in
 * the period, and the first migraine started at least 30 days before today. A migraine's start
 * date is the date in the zone it was logged in. A shared domain module like `:features:attackdays`,
 * with no models, repositories or use cases of its own.
 */
object PatternsReadiness {
    const val MIGRAINES_NEEDED = 5
    const val DAYS_NEEDED = 30

    /** The months Insights open on, so the ones the paywall after the 5th migraine looks at. */
    const val DEFAULT_MONTHS = 6

    /** The last [months] months on [today], today included, as the doctor report counts them. */
    fun lastMonths(months: Int, today: LocalDate): ClosedRange<LocalDate> =
        today.minusMonths(months.toLong()).plusDays(1)..today

    /** How many more migraines must start in the period, when [startedInPeriod] did. */
    fun missingMigraines(startedInPeriod: Int): Int =
        (MIGRAINES_NEEDED - startedInPeriod).coerceAtLeast(0)

    /** How many more days must pass since the first migraine, which started on [firstStart]. */
    fun missingDays(firstStart: LocalDate?, today: LocalDate): Int {
        val since = firstStart?.let { ChronoUnit.DAYS.between(it, today).toInt() } ?: 0
        return (DAYS_NEEDED - since).coerceAtLeast(0)
    }

    /** Whether patterns show for migraines that started on [startDates], in the last [months]. */
    fun ready(
        startDates: List<LocalDate>,
        today: LocalDate,
        months: Int = DEFAULT_MONTHS
    ): Boolean {
        val period = lastMonths(months, today)
        return missingMigraines(startDates.count { it in period }) == 0 &&
            missingDays(startDates.minOrNull(), today) == 0
    }
}
