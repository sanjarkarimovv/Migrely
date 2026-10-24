package com.migrely.app.insights.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CurrentTimeDomainModel(val instant: Instant, val zone: ZoneId)

/** The dates the chosen period covers, both included (BRD D87). */
data class InsightsDatesDomainModel(val from: LocalDate, val to: LocalDate)

/**
 * Every record Insights may use. [periodDates] are the dates marked as period days; a date has no zone (BRD
 * EC-DAY-04).
 */
data class InsightsRecordsDomainModel(
    val attacks: List<InsightsAttackDomainModel>,
    val intakes: List<InsightsIntakeDomainModel>,
    val periodDates: Set<LocalDate>
)

/** [end] is null while the attack is in progress. [functionalImpact] 3 is "Bed rest". */
data class InsightsAttackDomainModel(
    val start: Instant,
    val zone: ZoneId,
    val end: Instant?,
    val functionalImpact: Int?,
    val triggers: List<InsightsTriggerDomainModel>
)

/** A trigger logged with an attack: a built-in one, or one of the user's own by its name (BRD D25, D89). */
sealed interface InsightsTriggerDomainModel {
    data class BuiltIn(val trigger: InsightsBuiltInTriggerDomainModel) : InsightsTriggerDomainModel

    data class Custom(val name: String) : InsightsTriggerDomainModel
}

/** The built-in triggers, in the order the app lists them. */
enum class InsightsBuiltInTriggerDomainModel {
    Stress,
    TooLittleSleep,
    TooMuchSleep,
    SkippedMeal,
    Dehydration,
    Alcohol,
    Caffeine,
    Period,
    Weather,
    BrightLightOrScreens,
    Smell,
    PhysicalEffort,
    Travel
}

data class InsightsIntakeDomainModel(
    val medicationName: String,
    val kind: InsightsMedicationKindDomainModel,
    val takenAt: Instant,
    val zone: ZoneId,
    val effect: InsightsEffectDomainModel?
)

enum class InsightsMedicationKindDomainModel {
    Acute,
    Preventive
}

/** The answer to "Did it help?"; null on an intake means it was not answered. */
enum class InsightsEffectDomainModel {
    NotAtAll,
    ALittle,
    ALot,
    Completely
}

sealed interface InsightsRecordsResultDomainModel {
    data class Loaded(val records: InsightsRecordsDomainModel) : InsightsRecordsResultDomainModel

    data object Failed : InsightsRecordsResultDomainModel
}

/**
 * What Insights says about the last [months] months (BRD D87–D92). The [trends] always cover the last 12 calendar
 * months and show even when there is not enough for the [patterns] (D88).
 */
data class InsightsDomainModel(
    val months: Int,
    val dates: InsightsDatesDomainModel,
    val patterns: InsightsPatternsDomainModel,
    val trends: List<InsightsMonthDomainModel>
)

sealed interface InsightsPatternsDomainModel {
    /**
     * Fewer than 5 migraines started in the period, or the first migraine was logged less than 30 days ago (BRD
     * EC-INS-01, D88). Each count is what is still missing, 0 when that part is met.
     */
    data class NotEnough(val missingMigraines: Int, val missingDays: Int) :
        InsightsPatternsDomainModel

    /**
     * The sections, each counting the [migraines] that started in the period. [cycle] is null when Track periods is off
     * or fewer than two cycles started in the period (EC-INS-02, EC-INS-03).
     */
    data class Enough(
        val migraines: Int,
        val triggers: List<InsightsTriggerCountDomainModel>,
        val weekdays: InsightsWeekdaysDomainModel,
        val timesOfDay: InsightsTimesOfDayDomainModel,
        val cycle: InsightsCycleDomainModel?,
        val medications: List<InsightsMedicationDomainModel>
    ) : InsightsPatternsDomainModel
}

/** [migraines] of the period's migraines have [trigger] logged with them (BRD D89). */
data class InsightsTriggerCountDomainModel(
    val trigger: InsightsTriggerDomainModel,
    val migraines: Int
)

data class InsightsWeekdayDomainModel(val day: DayOfWeek, val migraines: Int)

/** Monday to Sunday. [busiest] is null when no single day has the most (BRD D90). */
data class InsightsWeekdaysDomainModel(
    val days: List<InsightsWeekdayDomainModel>,
    val busiest: DayOfWeek?
)

/** Night 00–06, morning 06–12, afternoon 12–18 and evening 18–24 (BRD D90). */
enum class InsightsTimeOfDayDomainModel {
    Night,
    Morning,
    Afternoon,
    Evening
}

data class InsightsTimeOfDayCountDomainModel(
    val time: InsightsTimeOfDayDomainModel,
    val migraines: Int
)

/** Night to evening. [busiest] is null when no single time has the most (BRD D90). */
data class InsightsTimesOfDayDomainModel(
    val times: List<InsightsTimeOfDayCountDomainModel>,
    val busiest: InsightsTimeOfDayDomainModel?
)

/**
 * The days around a period (BRD D91): [migraines] of the period's migraines started on them, and they are [days] of the
 * period's [periodDays], for comparison. [cycles] is the number of cycles that started in the period.
 */
data class InsightsCycleDomainModel(
    val cycles: Int,
    val migraines: Int,
    val days: Int,
    val periodDays: Int
)

/** An attack medication taken in the period, with its answers to "Did it help?" (BRD D92). */
sealed interface InsightsMedicationDomainModel {
    val name: String

    /** [helped] of the [answered] intakes were "A lot" or "Completely". */
    data class Rated(override val name: String, val helped: Int, val answered: Int) :
        InsightsMedicationDomainModel

    /** Fewer than 3 answers so far. */
    data class NeedsAnswers(override val name: String, val answered: Int) :
        InsightsMedicationDomainModel
}

/**
 * One calendar month of the trends (BRD D92): the dates migraines touched, the migraines that started, the dates an
 * attack medication was taken, and the migraines that started with "Bed rest".
 */
data class InsightsMonthDomainModel(
    val month: YearMonth,
    val migraineDays: Int,
    val migraines: Int,
    val medicationDays: Int,
    val bedRestMigraines: Int
)

sealed interface InsightsResultDomainModel {
    data class Ready(val insights: InsightsDomainModel) : InsightsResultDomainModel

    data object Failed : InsightsResultDomainModel
}
