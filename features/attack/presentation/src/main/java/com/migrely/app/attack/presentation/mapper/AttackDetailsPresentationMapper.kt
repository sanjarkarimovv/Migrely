package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackDetailsScreenDomainModel
import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationModel
import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel
import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.presentation.model.SymptomPresentationModel
import com.migrely.app.attack.presentation.model.TriggerPresentationModel

/**
 * Builds the details screen's model. Each choice list offers every built-in choice, the user's active custom tags and
 * the archived custom tags this attack still uses, so a removed tag stays visible where it was chosen (EC-ATK-11).
 */
@Suppress("LongParameterList")
class AttackDetailsPresentationMapper(
    private val headLocationPresentationMapper: HeadLocationPresentationMapper,
    private val builtInSymptomPresentationMapper: BuiltInSymptomPresentationMapper,
    private val builtInTriggerPresentationMapper: BuiltInTriggerPresentationMapper,
    private val customTagPresentationMapper: CustomTagPresentationMapper,
    private val functionalImpactPresentationMapper: FunctionalImpactPresentationMapper,
    private val intensityPointPresentationMapper: IntensityPointPresentationMapper
) {
    fun toPresentation(screen: AttackDetailsScreenDomainModel): AttackDetailsPresentationModel {
        val details = screen.details
        val usedCustomSymptoms = details.symptoms
            .filterIsInstance<SymptomDomainModel.Custom>()
            .map(SymptomDomainModel.Custom::tag)
        val usedCustomTriggers = details.triggers
            .filterIsInstance<TriggerDomainModel.Custom>()
            .map(TriggerDomainModel.Custom::tag)
        return AttackDetailsPresentationModel(
            id = details.id,
            start = details.start,
            startZone = details.startZone,
            end = details.end,
            now = screen.now,
            intensityPoints = details.intensityPoints
                .sortedBy { it.at }
                .map(intensityPointPresentationMapper::toPresentation),
            peakIntensity = details.peakIntensity,
            selectedLocations = details.locations
                .map(headLocationPresentationMapper::toPresentation)
                .toSet(),
            symptomChoices =
            BuiltInSymptomPresentationModel.entries.map(SymptomPresentationModel::BuiltIn) +
                customChoices(
                    screen.customSymptoms,
                    usedCustomSymptoms
                ).map(SymptomPresentationModel::Custom),
            selectedSymptoms = details.symptoms.map(::toPresentation).toSet(),
            triggerChoices =
            BuiltInTriggerPresentationModel.entries.map(TriggerPresentationModel::BuiltIn) +
                customChoices(
                    screen.customTriggers,
                    usedCustomTriggers
                ).map(TriggerPresentationModel::Custom),
            selectedTriggers = details.triggers.map(::toPresentation).toSet(),
            functionalImpact = details.functionalImpact
                ?.let(functionalImpactPresentationMapper::toPresentation),
            note = details.note
        )
    }

    private fun customChoices(
        active: List<CustomTagDomainModel>,
        usedHere: List<CustomTagDomainModel>
    ): List<CustomTagPresentationModel> = (active + usedHere)
        .distinctBy(CustomTagDomainModel::id)
        .map(customTagPresentationMapper::toPresentation)

    private fun toPresentation(symptom: SymptomDomainModel): SymptomPresentationModel =
        when (symptom) {
            is SymptomDomainModel.BuiltIn -> SymptomPresentationModel.BuiltIn(
                builtInSymptomPresentationMapper.toPresentation(symptom.symptom)
            )

            is SymptomDomainModel.Custom -> SymptomPresentationModel.Custom(
                customTagPresentationMapper.toPresentation(symptom.tag)
            )
        }

    private fun toPresentation(trigger: TriggerDomainModel): TriggerPresentationModel =
        when (trigger) {
            is TriggerDomainModel.BuiltIn -> TriggerPresentationModel.BuiltIn(
                builtInTriggerPresentationMapper.toPresentation(trigger.trigger)
            )

            is TriggerDomainModel.Custom -> TriggerPresentationModel.Custom(
                customTagPresentationMapper.toPresentation(trigger.tag)
            )
        }
}
