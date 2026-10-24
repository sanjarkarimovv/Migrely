package com.migrely.app.insights.ui.model

import java.time.DayOfWeek

sealed interface InsightsViewStateUiModel {
    data object Loading : InsightsViewStateUiModel

    data class Ready(val insights: InsightsUiModel) : InsightsViewStateUiModel

    data object Unavailable : InsightsViewStateUiModel
}

/** Insights for the last [months] months, and the last 12 calendar months, oldest first. */
data class InsightsUiModel(
    val months: Int,
    val patterns: InsightsPatternsUiModel,
    val trends: List<InsightsMonthUiModel>
)

sealed interface InsightsPatternsUiModel {
    data class NotEnough(val missingMigraines: Int, val missingDays: Int) : InsightsPatternsUiModel

    /** Every count is out of the [migraines] that started in the period. */
    data class Enough(
        val migraines: Int,
        val triggers: List<InsightsTriggerCountUiModel>,
        val weekdays: List<InsightsWeekdayBarUiModel>,
        val busiestWeekday: DayOfWeek?,
        val timesOfDay: List<InsightsTimeOfDayBarUiModel>,
        val busiestTimeOfDay: InsightsTimeOfDayUiModel?,
        val cycle: InsightsCycleUiModel?,
        val medications: List<InsightsMedicationUiModel>
    ) : InsightsPatternsUiModel
}

data class InsightsTriggerCountUiModel(
    val trigger: InsightsTriggerUiModel,
    val migraines: Int,
    val percent: Int
)

/** A built-in trigger, named from this module's strings, or one of the user's own by its name. */
sealed interface InsightsTriggerUiModel {
    data class BuiltIn(val trigger: InsightsBuiltInTriggerUiModel) : InsightsTriggerUiModel

    data class Custom(val name: String) : InsightsTriggerUiModel
}

enum class InsightsBuiltInTriggerUiModel {
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

/** [fraction] is the bar's length next to the longest bar, from 0 to 1. */
data class InsightsWeekdayBarUiModel(val day: DayOfWeek, val migraines: Int, val fraction: Float)

enum class InsightsTimeOfDayUiModel {
    Night,
    Morning,
    Afternoon,
    Evening
}

/** [fraction] is the bar's length next to the longest bar, from 0 to 1. */
data class InsightsTimeOfDayBarUiModel(
    val time: InsightsTimeOfDayUiModel,
    val migraines: Int,
    val fraction: Float
)

data class InsightsCycleUiModel(val migraines: Int, val percent: Int, val daysPercent: Int)

sealed interface InsightsMedicationUiModel {
    val name: String

    data class Rated(
        override val name: String,
        val helped: Int,
        val answered: Int,
        val percent: Int
    ) : InsightsMedicationUiModel

    data class NeedsAnswers(override val name: String, val answered: Int) :
        InsightsMedicationUiModel
}

/** One month of the trends; [fraction] is its migraine-day bar next to the longest of the 12. */
data class InsightsMonthUiModel(
    val year: Int,
    val month: Int,
    val migraineDays: Int,
    val migraines: Int,
    val medicationDays: Int,
    val bedRestMigraines: Int,
    val fraction: Float
)
