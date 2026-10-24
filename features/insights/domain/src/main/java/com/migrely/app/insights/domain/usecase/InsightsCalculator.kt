package com.migrely.app.insights.domain.usecase

import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.insights.domain.model.InsightsAttackDomainModel
import com.migrely.app.insights.domain.model.InsightsBuiltInTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsCycleDomainModel
import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import com.migrely.app.insights.domain.model.InsightsEffectDomainModel
import com.migrely.app.insights.domain.model.InsightsIntakeDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationKindDomainModel
import com.migrely.app.insights.domain.model.InsightsMonthDomainModel
import com.migrely.app.insights.domain.model.InsightsPatternsDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayCountDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayDomainModel
import com.migrely.app.insights.domain.model.InsightsTimesOfDayDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerCountDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsWeekdayDomainModel
import com.migrely.app.insights.domain.model.InsightsWeekdaysDomainModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val MIGRAINES_NEEDED = 5
private const val DAYS_NEEDED = 30
private const val TOP_TRIGGERS = 5
private const val CYCLES_NEEDED = 2
private const val ANSWERS_NEEDED = 3
private const val BED_REST = 3
private const val TREND_MONTHS = 12L
private const val HOURS_PER_TIME_OF_DAY = 6

private val PERIOD_TRIGGER = InsightsTriggerDomainModel.BuiltIn(
    InsightsBuiltInTriggerDomainModel.Period
)

/** Built-ins in the app's order, then the user's own by name; the order of ties in the triggers list. */
private val TRIGGER_TIES = compareBy<InsightsTriggerDomainModel> {
    when (it) {
        is InsightsTriggerDomainModel.BuiltIn -> it.trigger.ordinal
        is InsightsTriggerDomainModel.Custom -> InsightsBuiltInTriggerDomainModel.entries.size
    }
}.thenBy(String.CASE_INSENSITIVE_ORDER) {
    (it as? InsightsTriggerDomainModel.Custom)?.name.orEmpty()
}

/** The window around a cycle's start (BRD D91): two days before it, through its third day (start + 2). */
private const val DAYS_BEFORE_CYCLE = 2L
private const val DAYS_AFTER_CYCLE_START = 2L

/**
 * Insights by the app's own rules (BRD D57, D87–D92). A migraine belongs to the dates and hours of the zone it was
 * logged in, and a medication to the date of the zone it was taken in. "Started in the period" means the start date,
 * in that zone, falls within [dates]. [now] ends an attack still in progress.
 */
class InsightsCalculator(private val dates: InsightsDatesDomainModel, private val now: Instant) {
    private val period = dates.from..dates.to
    private val periodDays = ChronoUnit.DAYS.between(dates.from, dates.to).toInt() + 1

    /** The sections, or what is still missing before they can show (BRD EC-INS-01, D88). */
    fun patterns(
        records: InsightsRecordsDomainModel,
        periodTracking: Boolean
    ): InsightsPatternsDomainModel = notEnough(records) ?: InsightsPatternsDomainModel.Enough(
        migraines = started(records).size,
        triggers = triggers(records, periodTracking),
        weekdays = weekdays(records),
        timesOfDay = timesOfDay(records),
        cycle = if (periodTracking) cycle(records) else null,
        medications = medications(records)
    )

    /**
     * Null once at least 5 migraines started in the period and the first migraine started at least 30 days before
     * today; otherwise how many migraines and days are still missing (BRD D88).
     */
    fun notEnough(records: InsightsRecordsDomainModel): InsightsPatternsDomainModel.NotEnough? {
        val missingMigraines = (MIGRAINES_NEEDED - started(records).size).coerceAtLeast(0)
        val daysSinceFirst = records.attacks.minByOrNull { it.start }
            ?.let { ChronoUnit.DAYS.between(startDate(it), dates.to).toInt() } ?: 0
        val missingDays = (DAYS_NEEDED - daysSinceFirst).coerceAtLeast(0)
        return if (missingMigraines == 0 && missingDays == 0) {
            null
        } else {
            InsightsPatternsDomainModel.NotEnough(missingMigraines, missingDays)
        }
    }

    /**
     * The five triggers logged with the most of the period's migraines (BRD D89). Each counts once a migraine. A trigger
     * of the user's own stays apart from a built-in one of the same name; its names that differ only in case are one
     * trigger, shown as first found, since such names are unique ignoring case. Ties, at the fifth place too, go to the
     * built-ins in the app's order, then to the user's own by name. With Track periods off, the built-in "Period" is left
     * out (EC-INS-03).
     */
    fun triggers(
        records: InsightsRecordsDomainModel,
        periodTracking: Boolean
    ): List<InsightsTriggerCountDomainModel> = started(records)
        .flatMap { attack ->
            attack.triggers.filter { periodTracking || it != PERIOD_TRIGGER }.distinctBy(::sameAs)
        }
        .groupBy(::sameAs)
        .values
        .map { same -> InsightsTriggerCountDomainModel(same.first(), same.size) }
        .sortedWith(
            compareByDescending(InsightsTriggerCountDomainModel::migraines)
                .thenBy(TRIGGER_TIES, InsightsTriggerCountDomainModel::trigger)
        )
        .take(TOP_TRIGGERS)

    /** The period's migraines by the weekday they started on, Monday first (BRD D90). */
    fun weekdays(records: InsightsRecordsDomainModel): InsightsWeekdaysDomainModel {
        val counts = started(records).groupingBy { startTime(it).dayOfWeek }.eachCount()
        val days = DayOfWeek.entries.associateWith { counts[it] ?: 0 }
        return InsightsWeekdaysDomainModel(
            days.map { (day, migraines) -> InsightsWeekdayDomainModel(day, migraines) },
            busiestOf(days)
        )
    }

    /** The period's migraines by the hour they started: night, morning, afternoon, evening (BRD D90). */
    fun timesOfDay(records: InsightsRecordsDomainModel): InsightsTimesOfDayDomainModel {
        val counts = started(records).groupingBy {
            InsightsTimeOfDayDomainModel.entries[startTime(it).hour / HOURS_PER_TIME_OF_DAY]
        }.eachCount()
        val times = InsightsTimeOfDayDomainModel.entries.associateWith { counts[it] ?: 0 }
        return InsightsTimesOfDayDomainModel(
            times.map { (time, migraines) -> InsightsTimeOfDayCountDomainModel(time, migraines) },
            busiestOf(times)
        )
    }

    /**
     * The migraines that started in the days around a period, and how many of the period's days those are (BRD D91).
     * A cycle starts on a marked date whose previous date is not marked. Its window runs from two days before that
     * date through its third day; windows are counted only within the period, those of cycles that started just before
     * or after it included. Null with fewer than two cycles started in the period (EC-INS-02).
     */
    fun cycle(records: InsightsRecordsDomainModel): InsightsCycleDomainModel? {
        val marked = records.periodDates
        val starts = marked.filter { it.minusDays(1) !in marked }
        val cycles = starts.count(period::contains)
        if (cycles < CYCLES_NEEDED) return null
        val windowDates = starts.flatMap { start ->
            (-DAYS_BEFORE_CYCLE..DAYS_AFTER_CYCLE_START).map(start::plusDays)
        }.filter(period::contains).toSet()
        return InsightsCycleDomainModel(
            cycles = cycles,
            migraines = started(records).count { startDate(it) in windowDates },
            days = windowDates.size,
            periodDays = periodDays
        )
    }

    /**
     * Each attack medication taken in the period, the most taken first, with how often it helped "A lot" or
     * "Completely" once it has 3 answers (BRD D92). Preventive medications are left out.
     */
    fun medications(records: InsightsRecordsDomainModel): List<InsightsMedicationDomainModel> =
        records.intakes
            .filter { it.kind == InsightsMedicationKindDomainModel.Acute && dateOf(it) in period }
            .groupBy { it.medicationName }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, List<InsightsIntakeDomainModel>>> {
                    it.value.size
                }.thenBy { it.key }
            )
            .map { (name, intakes) ->
                val answered = intakes.count { it.effect != null }
                if (answered < ANSWERS_NEEDED) {
                    InsightsMedicationDomainModel.NeedsAnswers(name, answered)
                } else {
                    InsightsMedicationDomainModel.Rated(name, intakes.count(::helped), answered)
                }
            }

    /**
     * The last 12 calendar months, this one included, oldest first (BRD D92): the dates migraines touched (to now for
     * one in progress), the migraines started, the dates an attack medication was taken, and bed-rest migraines.
     */
    fun trends(records: InsightsRecordsDomainModel): List<InsightsMonthDomainModel> {
        val current = YearMonth.from(dates.to)
        val migraineDates = records.attacks.flatMap(::touched).groupBy(YearMonth::from)
        val medicationDates = records.intakes
            .filter { it.kind == InsightsMedicationKindDomainModel.Acute }
            .map(::dateOf)
            .groupBy(YearMonth::from)
        val startedIn = records.attacks.groupBy { YearMonth.from(startDate(it)) }
        return (TREND_MONTHS - 1 downTo 0).map(current::minusMonths).map { month ->
            val started = startedIn[month].orEmpty()
            InsightsMonthDomainModel(
                month = month,
                migraineDays = migraineDates[month].orEmpty().toSet().size,
                migraines = started.size,
                medicationDays = medicationDates[month].orEmpty().toSet().size,
                bedRestMigraines = started.count { it.functionalImpact == BED_REST }
            )
        }
    }

    /** What makes two logged triggers the same one: the built-in itself, or the user's name ignoring case. */
    private fun sameAs(trigger: InsightsTriggerDomainModel): Any = when (trigger) {
        is InsightsTriggerDomainModel.BuiltIn -> trigger
        is InsightsTriggerDomainModel.Custom -> trigger.name.lowercase(Locale.ROOT)
    }

    private fun started(records: InsightsRecordsDomainModel) =
        records.attacks.filter { startDate(it) in period }

    private fun startTime(attack: InsightsAttackDomainModel): ZonedDateTime =
        attack.start.atZone(attack.zone)

    private fun startDate(attack: InsightsAttackDomainModel): LocalDate =
        startTime(attack).toLocalDate()

    private fun dateOf(intake: InsightsIntakeDomainModel): LocalDate =
        intake.takenAt.atZone(intake.zone).toLocalDate()

    private fun helped(intake: InsightsIntakeDomainModel) =
        intake.effect == InsightsEffectDomainModel.ALot ||
            intake.effect == InsightsEffectDomainModel.Completely

    private fun touched(attack: InsightsAttackDomainModel): List<LocalDate> {
        val range = AttackDays.datesTouched(attack.start, attack.zone, attack.end, now)
        return generateSequence(range.start) { it.plusDays(1) }
            .takeWhile { it <= range.endInclusive }
            .toList()
    }

    /** The one with the highest count, or null when it is shared. */
    private fun <T> busiestOf(counts: Map<T, Int>): T? {
        val most = counts.values.maxOrNull() ?: return null
        return counts.filterValues { it == most }.keys.singleOrNull()
    }
}
