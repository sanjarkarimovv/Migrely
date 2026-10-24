package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackDetailsPresentationModel
import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.model.SymptomPresentationModel
import com.migrely.app.attack.presentation.model.TriggerPresentationModel
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.SymptomUiModel
import com.migrely.app.attack.ui.model.TriggerUiModel

@Suppress("LongParameterList")
class AttackDetailsViewStateUiMapper(
    private val headLocationUiMapper: HeadLocationUiMapper,
    private val builtInSymptomUiMapper: BuiltInSymptomUiMapper,
    private val builtInTriggerUiMapper: BuiltInTriggerUiMapper,
    private val customTagUiMapper: CustomTagUiMapper,
    private val functionalImpactUiMapper: FunctionalImpactUiMapper
) {
    fun toUi(viewState: AttackDetailsViewState): AttackDetailsViewStateUiModel = when (viewState) {
        AttackDetailsViewState.Loading -> AttackDetailsViewStateUiModel.Loading
        is AttackDetailsViewState.Ready ->
            AttackDetailsViewStateUiModel.Ready(toUi(viewState.details))
        AttackDetailsViewState.NotFound -> AttackDetailsViewStateUiModel.NotFound
    }

    private fun toUi(details: AttackDetailsPresentationModel) = AttackDetailsUiModel(
        id = details.id,
        startEpochMillis = details.start.toEpochMilli(),
        startZoneId = details.startZone.id,
        endEpochMillis = details.end?.toEpochMilli(),
        nowEpochMillis = details.now.toEpochMilli(),
        intensityPoints = details.intensityPoints.map {
            IntensityPointUiModel(it.at.toEpochMilli(), it.value)
        },
        peakIntensity = details.peakIntensity,
        selectedLocations = details.selectedLocations.map(headLocationUiMapper::toUi).toSet(),
        symptomChoices = details.symptomChoices.map(::toUi),
        selectedSymptoms = details.selectedSymptoms.map(::toUi).toSet(),
        triggerChoices = details.triggerChoices.map(::toUi),
        selectedTriggers = details.selectedTriggers.map(::toUi).toSet(),
        functionalImpact = details.functionalImpact?.let(functionalImpactUiMapper::toUi),
        note = details.note,
        intakes = details.intakes.map { intake ->
            AttackIntakeUiModel(
                id = intake.id,
                medicationId = intake.medicationId,
                medicationName = intake.medicationName,
                dose = intake.dose,
                takenAtEpochMillis = intake.takenAt.toEpochMilli(),
                zoneId = intake.zone.id
            )
        }
    )

    private fun toUi(symptom: SymptomPresentationModel): SymptomUiModel = when (symptom) {
        is SymptomPresentationModel.BuiltIn ->
            SymptomUiModel.BuiltIn(builtInSymptomUiMapper.toUi(symptom.symptom))
        is SymptomPresentationModel.Custom ->
            SymptomUiModel.Custom(customTagUiMapper.toUi(symptom.tag))
    }

    private fun toUi(trigger: TriggerPresentationModel): TriggerUiModel = when (trigger) {
        is TriggerPresentationModel.BuiltIn ->
            TriggerUiModel.BuiltIn(builtInTriggerUiMapper.toUi(trigger.trigger))
        is TriggerPresentationModel.Custom ->
            TriggerUiModel.Custom(customTagUiMapper.toUi(trigger.tag))
    }
}
