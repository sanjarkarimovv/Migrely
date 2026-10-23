package com.migrely.app.home.ui.mapper

import com.migrely.app.home.presentation.model.AttackInProgressPresentationModel
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.presentation.model.PeakPresentationModel
import com.migrely.app.home.presentation.model.SkyPresentationModel
import com.migrely.app.home.presentation.model.TodayPresentationModel
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.AttackSummaryUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.PeakUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel

class HomeViewStateUiMapper {
    fun toUi(viewState: HomeViewState): HomeViewStateUiModel = when (viewState) {
        HomeViewState.Loading -> HomeViewStateUiModel.Loading
        is HomeViewState.Today -> HomeViewStateUiModel.Today(viewState.today.toUi())
        is HomeViewState.AttackInProgress -> HomeViewStateUiModel.AttackInProgress(
            viewState.attack.toUi()
        )
    }

    private fun TodayPresentationModel.toUi() = TodayUiModel(
        sky = when (sky) {
            SkyPresentationModel.Calm -> SkyUiModel.Calm
            SkyPresentationModel.Hazy -> SkyUiModel.Hazy
            SkyPresentationModel.Cloudy -> SkyUiModel.Cloudy
            SkyPresentationModel.Storm -> SkyUiModel.Storm
        },
        attackCount = attackCount,
        peak = when (peak) {
            PeakPresentationModel.Mild -> PeakUiModel.Mild
            PeakPresentationModel.Moderate -> PeakUiModel.Moderate
            PeakPresentationModel.Severe -> PeakUiModel.Severe
            PeakPresentationModel.NotRecorded -> PeakUiModel.NotRecorded
            null -> null
        },
        attacks = attacks.map { attack ->
            AttackSummaryUiModel(
                id = attack.id,
                startEpochMillis = attack.start.toEpochMilli(),
                startZoneId = attack.startZone.id,
                endEpochMillis = attack.end.toEpochMilli(),
                peakIntensity = attack.peakIntensity
            )
        }
    )

    private fun AttackInProgressPresentationModel.toUi() = AttackInProgressUiModel(
        id = id,
        startEpochMillis = start.toEpochMilli(),
        startZoneId = startZone.id,
        currentLevel = when (currentLevel) {
            IntensityLevelPresentationModel.Mild -> IntensityLevelUiModel.Mild
            IntensityLevelPresentationModel.Moderate -> IntensityLevelUiModel.Moderate
            IntensityLevelPresentationModel.Severe -> IntensityLevelUiModel.Severe
            null -> null
        },
        longAttackCheckDue = longAttackCheckDue
    )
}
