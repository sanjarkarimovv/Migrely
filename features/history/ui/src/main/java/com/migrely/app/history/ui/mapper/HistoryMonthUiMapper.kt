package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.DaySkyPresentationModel
import com.migrely.app.history.presentation.model.HistoryAttackPresentationModel
import com.migrely.app.history.presentation.model.HistoryDayPresentationModel
import com.migrely.app.history.presentation.model.HistoryMonthPresentationModel
import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.history.presentation.model.MonthStatsPresentationModel
import com.migrely.app.history.presentation.model.PeakPresentationModel
import com.migrely.app.history.presentation.model.ProgressPresentationModel
import com.migrely.app.history.ui.model.DaySkyUiModel
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryDayUiModel
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.MonthStatsUiModel
import com.migrely.app.history.ui.model.PainLevelUiModel
import com.migrely.app.history.ui.model.PeakUiModel
import com.migrely.app.history.ui.model.ProgressAgainstUiModel
import com.migrely.app.history.ui.model.ProgressTrendUiModel
import com.migrely.app.history.ui.model.ProgressUiModel

class HistoryMonthUiMapper {
    fun toUi(month: HistoryMonthPresentationModel) = HistoryMonthUiModel(
        year = month.month.year,
        month = month.month.monthValue,
        todayEpochDay = month.today.toEpochDay(),
        hasPreviousMonth = month.hasPreviousMonth,
        hasNextMonth = month.hasNextMonth,
        days = month.days.map { it.toUi() },
        attacks = month.attacks.map { it.toUi() },
        stats = month.stats.toUi()
    )

    private fun MonthStatsPresentationModel.toUi() = MonthStatsUiModel(
        migraines = migraines,
        migraineDays = migraineDays,
        painFreeDays = painFreeDays,
        averagePeak = when (averagePeak) {
            IntensityLevelPresentationModel.Mild -> PainLevelUiModel.Mild
            IntensityLevelPresentationModel.Moderate -> PainLevelUiModel.Moderate
            IntensityLevelPresentationModel.Severe -> PainLevelUiModel.Severe
            null -> null
        },
        attackMedicationDays = attackMedicationDays,
        chronic = chronic,
        progress = progress?.toUi()
    )

    private fun ProgressPresentationModel.toUi(): ProgressUiModel {
        val against = when (val compared = comparedWith) {
            is ProgressPresentationModel.ComparedWith.WholeMonth ->
                ProgressAgainstUiModel.WholeMonth(compared.month.year, compared.month.monthValue)
            ProgressPresentationModel.ComparedWith.SameDaysLastMonth ->
                ProgressAgainstUiModel.SameDaysLastMonth
        }
        return when (this) {
            is ProgressPresentationModel.Fewer -> ProgressUiModel(
                ProgressTrendUiModel.Fewer,
                days,
                against
            )
            is ProgressPresentationModel.More -> ProgressUiModel(
                ProgressTrendUiModel.More,
                days,
                against
            )
            is ProgressPresentationModel.Same -> ProgressUiModel(
                ProgressTrendUiModel.Same,
                0,
                against
            )
        }
    }

    private fun HistoryDayPresentationModel.toUi() = HistoryDayUiModel(
        epochDay = date.toEpochDay(),
        sky = when (sky) {
            DaySkyPresentationModel.NothingLogged -> DaySkyUiModel.NothingLogged
            DaySkyPresentationModel.Future -> DaySkyUiModel.Future
            DaySkyPresentationModel.Clear -> DaySkyUiModel.Clear
            DaySkyPresentationModel.Hazy -> DaySkyUiModel.Hazy
            DaySkyPresentationModel.Cloudy -> DaySkyUiModel.Cloudy
            DaySkyPresentationModel.Storm -> DaySkyUiModel.Storm
            DaySkyPresentationModel.Rain -> DaySkyUiModel.Rain
        },
        peak = when (peak) {
            PeakPresentationModel.Mild -> PeakUiModel.Mild
            PeakPresentationModel.Moderate -> PeakUiModel.Moderate
            PeakPresentationModel.Severe -> PeakUiModel.Severe
            PeakPresentationModel.NotRecorded -> PeakUiModel.NotRecorded
            null -> null
        },
        attacks = attacks.map { it.toUi() },
        period = period,
        painFree = painFree,
        painFreeAllowed = painFreeAllowed
    )

    private fun HistoryAttackPresentationModel.toUi() = HistoryAttackUiModel(
        id = id,
        startEpochMillis = start.toEpochMilli(),
        startZoneId = startZone.id,
        endEpochMillis = end?.toEpochMilli(),
        peakIntensity = peakIntensity
    )
}
