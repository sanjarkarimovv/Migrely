package com.migrely.app.insights.presentation.mapper

import com.migrely.app.insights.domain.model.InsightsCycleDomainModel
import com.migrely.app.insights.domain.model.InsightsDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationDomainModel
import com.migrely.app.insights.domain.model.InsightsMonthDomainModel
import com.migrely.app.insights.domain.model.InsightsPatternsDomainModel
import com.migrely.app.insights.domain.model.InsightsResultDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayDomainModel
import com.migrely.app.insights.presentation.model.InsightsCyclePresentationModel
import com.migrely.app.insights.presentation.model.InsightsMedicationPresentationModel
import com.migrely.app.insights.presentation.model.InsightsMonthPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPatternsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPercentPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayCountPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerCountPresentationModel
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.presentation.model.InsightsWeekdayPresentationModel
import kotlin.math.roundToInt

private const val HUNDRED = 100.0
private const val ALL = 100

/** Insights as the screen shows them, each share a whole percent of its total. */
class InsightsPresentationMapper(
    private val insightsTriggerPresentationMapper: InsightsTriggerPresentationMapper
) {
    fun toPresentation(result: InsightsResultDomainModel): InsightsViewState = when (result) {
        is InsightsResultDomainModel.Ready -> InsightsViewState.Ready(insightsOf(result.insights))
        InsightsResultDomainModel.Failed -> InsightsViewState.Unavailable
    }

    private fun insightsOf(insights: InsightsDomainModel) = InsightsPresentationModel(
        months = insights.months,
        patterns = when (val patterns = insights.patterns) {
            is InsightsPatternsDomainModel.NotEnough ->
                InsightsPatternsPresentationModel.NotEnough(
                    patterns.missingMigraines,
                    patterns.missingDays
                )
            is InsightsPatternsDomainModel.Enough -> enoughOf(patterns)
        },
        trends = insights.trends.map(::monthOf)
    )

    private fun enoughOf(patterns: InsightsPatternsDomainModel.Enough) =
        InsightsPatternsPresentationModel.Enough(
            migraines = patterns.migraines,
            triggers = patterns.triggers.map {
                InsightsTriggerCountPresentationModel(
                    insightsTriggerPresentationMapper.toPresentation(it.trigger),
                    it.migraines,
                    percentOf(it.migraines, patterns.migraines)
                )
            },
            weekdays = patterns.weekdays.days.map {
                InsightsWeekdayPresentationModel(it.day, it.migraines)
            },
            busiestWeekday = patterns.weekdays.busiest,
            timesOfDay = patterns.timesOfDay.times.map {
                InsightsTimeOfDayCountPresentationModel(timeOf(it.time), it.migraines)
            },
            busiestTimeOfDay = patterns.timesOfDay.busiest?.let(::timeOf),
            cycle = patterns.cycle?.let { cycleOf(it, patterns.migraines) },
            medications = patterns.medications.map(::medicationOf)
        )

    private fun timeOf(time: InsightsTimeOfDayDomainModel) = when (time) {
        InsightsTimeOfDayDomainModel.Night -> InsightsTimeOfDayPresentationModel.Night
        InsightsTimeOfDayDomainModel.Morning -> InsightsTimeOfDayPresentationModel.Morning
        InsightsTimeOfDayDomainModel.Afternoon -> InsightsTimeOfDayPresentationModel.Afternoon
        InsightsTimeOfDayDomainModel.Evening -> InsightsTimeOfDayPresentationModel.Evening
    }

    private fun cycleOf(cycle: InsightsCycleDomainModel, migraines: Int) =
        InsightsCyclePresentationModel(
            migraines = cycle.migraines,
            percent = percentOf(cycle.migraines, migraines),
            days = cycle.days,
            periodDays = cycle.periodDays,
            daysPercent = percentOf(cycle.days, cycle.periodDays)
        )

    private fun medicationOf(medication: InsightsMedicationDomainModel) = when (medication) {
        is InsightsMedicationDomainModel.Rated -> InsightsMedicationPresentationModel.Rated(
            medication.name,
            medication.helped,
            medication.answered,
            percentOf(medication.helped, medication.answered)
        )
        is InsightsMedicationDomainModel.NeedsAnswers ->
            InsightsMedicationPresentationModel.NeedsAnswers(medication.name, medication.answered)
    }

    private fun monthOf(month: InsightsMonthDomainModel) = InsightsMonthPresentationModel(
        month = month.month,
        migraineDays = month.migraineDays,
        migraines = month.migraines,
        medicationDays = month.medicationDays,
        bedRestMigraines = month.bedRestMigraines
    )

    /** Rounded to a whole percent, but never to 0 for some or to 100 for less than all. */
    private fun percentOf(count: Int, total: Int): InsightsPercentPresentationModel {
        val percent = if (total == 0) 0 else (count * HUNDRED / total).roundToInt()
        return when {
            percent == 0 && count > 0 -> InsightsPercentPresentationModel.BelowOne
            percent == ALL && count < total -> InsightsPercentPresentationModel.AboveNinetyNine
            else -> InsightsPercentPresentationModel.Whole(percent)
        }
    }
}
