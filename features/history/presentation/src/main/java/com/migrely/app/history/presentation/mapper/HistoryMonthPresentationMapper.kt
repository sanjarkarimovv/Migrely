package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.HistoryDayDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.presentation.model.DaySkyPresentationModel
import com.migrely.app.history.presentation.model.HistoryDayPresentationModel
import com.migrely.app.history.presentation.model.HistoryMonthPresentationModel
import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.history.presentation.model.PeakPresentationModel
import java.time.LocalDate
import java.time.YearMonth

/**
 * Turns a month into calendar days, with the sky rule of today's card (BRD EC-UX-09): 1–3 hazy, 4–6 cloudy, 7–10
 * storm, and an attack without a recorded level counts as cloudy. A day with an attack in progress shows rain
 * (BRD HIS-F1). A day without attacks is clear only when marked pain-free; otherwise up to today it is "nothing logged",
 * later "future" (G6, D47).
 */
class HistoryMonthPresentationMapper(
    private val historyAttackPresentationMapper: HistoryAttackPresentationMapper,
    private val intensityLevelPresentationMapper: IntensityLevelPresentationMapper,
    private val monthStatsPresentationMapper: MonthStatsPresentationMapper
) {
    fun toPresentation(month: HistoryMonthDomainModel) = HistoryMonthPresentationModel(
        month = month.month,
        today = month.today,
        hasPreviousMonth = month.month > month.earliestMonth,
        hasNextMonth = month.month < YearMonth.from(month.today),
        days = month.days.map { day -> day.toPresentation(month.today) },
        attacks = month.attacks.map(historyAttackPresentationMapper::toPresentation),
        stats = monthStatsPresentationMapper.toPresentation(month.stats)
    )

    private fun HistoryDayDomainModel.toPresentation(
        today: LocalDate
    ): HistoryDayPresentationModel {
        val recordedLevel = attacks.mapNotNull { it.peakIntensity }
            .maxOrNull()
            ?.let(intensityLevelPresentationMapper::toPresentation)
        val skyLevel = listOfNotNull(
            recordedLevel,
            IntensityLevelPresentationModel.Moderate.takeIf {
                attacks.any { it.peakIntensity == null }
            }
        ).maxOrNull()
        val sky = when {
            attacks.any { it.end == null } -> DaySkyPresentationModel.Rain
            skyLevel != null -> skyLevel.toSky()
            painFree -> DaySkyPresentationModel.Clear
            date.isAfter(today) -> DaySkyPresentationModel.Future
            else -> DaySkyPresentationModel.NothingLogged
        }
        return HistoryDayPresentationModel(
            date = date,
            sky = sky,
            peak = if (attacks.isEmpty()) null else recordedLevel.toPeak(),
            attacks = attacks.map(historyAttackPresentationMapper::toPresentation),
            period = period,
            painFree = painFree,
            painFreeAllowed = attacks.isEmpty()
        )
    }

    private fun IntensityLevelPresentationModel.toSky(): DaySkyPresentationModel = when (this) {
        IntensityLevelPresentationModel.Mild -> DaySkyPresentationModel.Hazy
        IntensityLevelPresentationModel.Moderate -> DaySkyPresentationModel.Cloudy
        IntensityLevelPresentationModel.Severe -> DaySkyPresentationModel.Storm
    }

    private fun IntensityLevelPresentationModel?.toPeak(): PeakPresentationModel = when (this) {
        IntensityLevelPresentationModel.Mild -> PeakPresentationModel.Mild
        IntensityLevelPresentationModel.Moderate -> PeakPresentationModel.Moderate
        IntensityLevelPresentationModel.Severe -> PeakPresentationModel.Severe
        null -> PeakPresentationModel.NotRecorded
    }
}
