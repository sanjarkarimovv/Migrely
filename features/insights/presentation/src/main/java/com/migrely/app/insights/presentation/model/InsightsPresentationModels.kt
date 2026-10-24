package com.migrely.app.insights.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.DayOfWeek
import java.time.YearMonth

sealed interface InsightsViewState {
    data object Loading : InsightsViewState

    data class Ready(val insights: InsightsPresentationModel) : InsightsViewState

    /** The records cannot be read right now. */
    data object Unavailable : InsightsViewState
}

/** Insights shows no messages of its own: a failure shows as [InsightsViewState.Unavailable]. */
sealed interface InsightsPresentationNotification : PresentationNotification

/** Insights for the last [months] months, and the [trends] of the last 12 calendar months (BRD D87, D92). */
data class InsightsPresentationModel(
    val months: Int,
    val patterns: InsightsPatternsPresentationModel,
    val trends: List<InsightsMonthPresentationModel>
)

sealed interface InsightsPatternsPresentationModel {
    /** What is still missing before the sections show, 0 when that part is met (BRD D88). */
    data class NotEnough(val missingMigraines: Int, val missingDays: Int) :
        InsightsPatternsPresentationModel

    /** Every count is out of the [migraines] that started in the period. */
    data class Enough(
        val migraines: Int,
        val triggers: List<InsightsTriggerPresentationModel>,
        val weekdays: List<InsightsWeekdayPresentationModel>,
        val busiestWeekday: DayOfWeek?,
        val timesOfDay: List<InsightsTimeOfDayCountPresentationModel>,
        val busiestTimeOfDay: InsightsTimeOfDayPresentationModel?,
        val cycle: InsightsCyclePresentationModel?,
        val medications: List<InsightsMedicationPresentationModel>
    ) : InsightsPatternsPresentationModel
}

/** [percent] is [migraines] as a share of the period's migraines, rounded. */
data class InsightsTriggerPresentationModel(val name: String, val migraines: Int, val percent: Int)

data class InsightsWeekdayPresentationModel(val day: DayOfWeek, val migraines: Int)

enum class InsightsTimeOfDayPresentationModel {
    Night,
    Morning,
    Afternoon,
    Evening
}

data class InsightsTimeOfDayCountPresentationModel(
    val time: InsightsTimeOfDayPresentationModel,
    val migraines: Int
)

/**
 * [migraines] started in the days around a period, [percent] of the period's migraines; those days are [daysPercent]
 * of the period's days (BRD D91).
 */
data class InsightsCyclePresentationModel(
    val migraines: Int,
    val percent: Int,
    val daysPercent: Int
)

sealed interface InsightsMedicationPresentationModel {
    val name: String

    /** [helped] of [answered] answers were "A lot" or "Completely", [percent] rounded (BRD D92). */
    data class Rated(
        override val name: String,
        val helped: Int,
        val answered: Int,
        val percent: Int
    ) : InsightsMedicationPresentationModel

    /** Fewer than 3 answers so far. */
    data class NeedsAnswers(override val name: String, val answered: Int) :
        InsightsMedicationPresentationModel
}

data class InsightsMonthPresentationModel(
    val month: YearMonth,
    val migraineDays: Int,
    val migraines: Int,
    val medicationDays: Int,
    val bedRestMigraines: Int
)
