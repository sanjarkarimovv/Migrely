package com.migrely.app.insights.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.insights.presentation.model.InsightsMedicationPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPatternsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationNotification
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayPresentationModel
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.ui.model.InsightsCycleUiModel
import com.migrely.app.insights.ui.model.InsightsMedicationUiModel
import com.migrely.app.insights.ui.model.InsightsMonthUiModel
import com.migrely.app.insights.ui.model.InsightsPatternsUiModel
import com.migrely.app.insights.ui.model.InsightsTimeOfDayBarUiModel
import com.migrely.app.insights.ui.model.InsightsTimeOfDayUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerCountUiModel
import com.migrely.app.insights.ui.model.InsightsUiModel
import com.migrely.app.insights.ui.model.InsightsViewStateUiModel
import com.migrely.app.insights.ui.model.InsightsWeekdayBarUiModel

/** Insights as drawn: each bar's length is its count next to the highest count of its chart. */
class InsightsViewStateUiMapper(private val insightsTriggerUiMapper: InsightsTriggerUiMapper) {
    fun toUi(viewState: InsightsViewState): InsightsViewStateUiModel = when (viewState) {
        InsightsViewState.Loading -> InsightsViewStateUiModel.Loading
        is InsightsViewState.Ready -> InsightsViewStateUiModel.Ready(insightsOf(viewState.insights))
        InsightsViewState.Unavailable -> InsightsViewStateUiModel.Unavailable
    }

    private fun insightsOf(insights: InsightsPresentationModel): InsightsUiModel {
        val longest = insights.trends.maxOfOrNull { it.migraineDays } ?: 0
        return InsightsUiModel(
            months = insights.months,
            patterns = when (val patterns = insights.patterns) {
                is InsightsPatternsPresentationModel.NotEnough -> InsightsPatternsUiModel.NotEnough(
                    patterns.missingMigraines,
                    patterns.missingDays
                )
                is InsightsPatternsPresentationModel.Enough -> enoughOf(patterns)
            },
            trends = insights.trends.map {
                InsightsMonthUiModel(
                    year = it.month.year,
                    month = it.month.monthValue,
                    migraineDays = it.migraineDays,
                    migraines = it.migraines,
                    medicationDays = it.medicationDays,
                    bedRestMigraines = it.bedRestMigraines,
                    fraction = fractionOf(it.migraineDays, longest)
                )
            }
        )
    }

    private fun enoughOf(
        patterns: InsightsPatternsPresentationModel.Enough
    ): InsightsPatternsUiModel {
        val busiestDay = patterns.weekdays.maxOfOrNull { it.migraines } ?: 0
        val busiestTime = patterns.timesOfDay.maxOfOrNull { it.migraines } ?: 0
        return InsightsPatternsUiModel.Enough(
            migraines = patterns.migraines,
            triggers = patterns.triggers.map {
                InsightsTriggerCountUiModel(
                    insightsTriggerUiMapper.toUi(it.trigger),
                    it.migraines,
                    it.percent
                )
            },
            weekdays = patterns.weekdays.map {
                InsightsWeekdayBarUiModel(
                    it.day,
                    it.migraines,
                    fractionOf(it.migraines, busiestDay)
                )
            },
            busiestWeekday = patterns.busiestWeekday,
            timesOfDay = patterns.timesOfDay.map {
                InsightsTimeOfDayBarUiModel(
                    timeOf(it.time),
                    it.migraines,
                    fractionOf(it.migraines, busiestTime)
                )
            },
            busiestTimeOfDay = patterns.busiestTimeOfDay?.let(::timeOf),
            cycle = patterns.cycle?.let {
                InsightsCycleUiModel(it.migraines, it.percent, it.daysPercent)
            },
            medications = patterns.medications.map {
                when (it) {
                    is InsightsMedicationPresentationModel.Rated ->
                        InsightsMedicationUiModel.Rated(it.name, it.helped, it.answered, it.percent)
                    is InsightsMedicationPresentationModel.NeedsAnswers ->
                        InsightsMedicationUiModel.NeedsAnswers(it.name, it.answered)
                }
            }
        )
    }

    private fun timeOf(time: InsightsTimeOfDayPresentationModel) = when (time) {
        InsightsTimeOfDayPresentationModel.Night -> InsightsTimeOfDayUiModel.Night
        InsightsTimeOfDayPresentationModel.Morning -> InsightsTimeOfDayUiModel.Morning
        InsightsTimeOfDayPresentationModel.Afternoon -> InsightsTimeOfDayUiModel.Afternoon
        InsightsTimeOfDayPresentationModel.Evening -> InsightsTimeOfDayUiModel.Evening
    }

    private fun fractionOf(count: Int, highest: Int) =
        if (highest == 0) 0f else count.toFloat() / highest
}

/** Insights has no notifications (InsightsPresentationNotification has no members), so this is never asked. */
class InsightsNotificationUiMapper : NotificationUiMapper<InsightsPresentationNotification> {
    override fun toUi(notification: InsightsPresentationNotification): UiNotification =
        UiNotification { }
}
