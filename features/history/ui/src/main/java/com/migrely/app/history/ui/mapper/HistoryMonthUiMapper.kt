package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.DaySkyPresentationModel
import com.migrely.app.history.presentation.model.HistoryAttackPresentationModel
import com.migrely.app.history.presentation.model.HistoryDayPresentationModel
import com.migrely.app.history.presentation.model.HistoryMonthPresentationModel
import com.migrely.app.history.presentation.model.PeakPresentationModel
import com.migrely.app.history.ui.model.DaySkyUiModel
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryDayUiModel
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.PeakUiModel

class HistoryMonthUiMapper {
    fun toUi(month: HistoryMonthPresentationModel) = HistoryMonthUiModel(
        year = month.month.year,
        month = month.month.monthValue,
        todayEpochDay = month.today.toEpochDay(),
        hasPreviousMonth = month.hasPreviousMonth,
        hasNextMonth = month.hasNextMonth,
        days = month.days.map { it.toUi() },
        attacks = month.attacks.map { it.toUi() }
    )

    private fun HistoryDayPresentationModel.toUi() = HistoryDayUiModel(
        epochDay = date.toEpochDay(),
        sky = when (sky) {
            DaySkyPresentationModel.NothingLogged -> DaySkyUiModel.NothingLogged
            DaySkyPresentationModel.Future -> DaySkyUiModel.Future
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
        attacks = attacks.map { it.toUi() }
    )

    private fun HistoryAttackPresentationModel.toUi() = HistoryAttackUiModel(
        id = id,
        startEpochMillis = start.toEpochMilli(),
        startZoneId = startZone.id,
        endEpochMillis = end?.toEpochMilli(),
        peakIntensity = peakIntensity
    )
}
