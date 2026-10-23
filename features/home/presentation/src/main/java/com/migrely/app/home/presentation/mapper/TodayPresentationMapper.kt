package com.migrely.app.home.presentation.mapper

import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.presentation.model.AttackInProgressPresentationModel
import com.migrely.app.home.presentation.model.AttackSummaryPresentationModel
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.presentation.model.PeakPresentationModel
import com.migrely.app.home.presentation.model.SkyPresentationModel
import com.migrely.app.home.presentation.model.TodayPresentationModel

/**
 * Turns today's data into what the home screen shows. Sky rule (BRD EC-UX-09): 1–3 hazy, 4–6 cloudy, 7–10 storm, and
 * an attack without recorded intensity counts as cloudy. Nothing logged is calm, never clear.
 */
class TodayPresentationMapper(
    private val intensityLevelPresentationMapper: IntensityLevelPresentationMapper
) {
    fun toPresentation(today: TodayDomainModel): HomeViewState = when (today) {
        TodayDomainModel.Calm -> HomeViewState.Today(
            TodayPresentationModel(sky = SkyPresentationModel.Calm, attackCount = 0, peak = null)
        )

        is TodayDomainModel.AttackInProgress -> HomeViewState.AttackInProgress(
            AttackInProgressPresentationModel(
                id = today.attack.id,
                start = today.attack.start,
                startZone = today.attack.startZone,
                currentLevel = today.attack.latestIntensityPoint?.value
                    ?.let(intensityLevelPresentationMapper::toPresentation),
                longAttackCheckDue = today.longAttackCheckDue
            )
        )

        is TodayDomainModel.AttacksEnded -> HomeViewState.Today(endedToday(today))
    }

    private fun endedToday(today: TodayDomainModel.AttacksEnded): TodayPresentationModel {
        val recordedLevel = today.highestRecordedIntensity?.let(
            intensityLevelPresentationMapper::toPresentation
        )
        val skyLevel = listOfNotNull(
            recordedLevel,
            IntensityLevelPresentationModel.Moderate.takeIf { today.anyIntensityNotRecorded }
        ).maxOrNull()
        return TodayPresentationModel(
            sky = skyLevel.toSky(),
            attackCount = today.count,
            peak = recordedLevel.toPeak(),
            attacks = today.attacks.map { attack ->
                AttackSummaryPresentationModel(
                    id = attack.id,
                    start = attack.start,
                    startZone = attack.startZone,
                    end = attack.end,
                    peakIntensity = attack.peakIntensity
                )
            }
        )
    }

    private fun IntensityLevelPresentationModel?.toSky(): SkyPresentationModel = when (this) {
        IntensityLevelPresentationModel.Mild -> SkyPresentationModel.Hazy
        IntensityLevelPresentationModel.Moderate -> SkyPresentationModel.Cloudy
        IntensityLevelPresentationModel.Severe -> SkyPresentationModel.Storm
        null -> SkyPresentationModel.Cloudy
    }

    private fun IntensityLevelPresentationModel?.toPeak(): PeakPresentationModel = when (this) {
        IntensityLevelPresentationModel.Mild -> PeakPresentationModel.Mild
        IntensityLevelPresentationModel.Moderate -> PeakPresentationModel.Moderate
        IntensityLevelPresentationModel.Severe -> PeakPresentationModel.Severe
        null -> PeakPresentationModel.NotRecorded
    }
}
