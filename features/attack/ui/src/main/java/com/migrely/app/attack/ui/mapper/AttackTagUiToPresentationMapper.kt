package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackTagPresentationModel
import com.migrely.app.attack.presentation.model.SymptomPresentationModel
import com.migrely.app.attack.presentation.model.TriggerPresentationModel
import com.migrely.app.attack.ui.model.AttackTagUiModel
import com.migrely.app.attack.ui.model.SymptomUiModel
import com.migrely.app.attack.ui.model.TriggerUiModel

class AttackTagUiToPresentationMapper(
    private val headLocationUiToPresentationMapper: HeadLocationUiToPresentationMapper,
    private val builtInSymptomUiToPresentationMapper: BuiltInSymptomUiToPresentationMapper,
    private val builtInTriggerUiToPresentationMapper: BuiltInTriggerUiToPresentationMapper,
    private val customTagUiToPresentationMapper: CustomTagUiToPresentationMapper
) {
    fun toPresentation(tag: AttackTagUiModel): AttackTagPresentationModel = when (tag) {
        is AttackTagUiModel.Location ->
            AttackTagPresentationModel.Location(
                headLocationUiToPresentationMapper
                    .toPresentation(tag.location)
            )

        is AttackTagUiModel.Symptom -> AttackTagPresentationModel.Symptom(
            when (val symptom = tag.symptom) {
                is SymptomUiModel.BuiltIn -> SymptomPresentationModel.BuiltIn(
                    builtInSymptomUiToPresentationMapper.toPresentation(symptom.symptom)
                )

                is SymptomUiModel.Custom ->
                    SymptomPresentationModel.Custom(
                        customTagUiToPresentationMapper
                            .toPresentation(symptom.tag)
                    )
            }
        )

        is AttackTagUiModel.Trigger -> AttackTagPresentationModel.Trigger(
            when (val trigger = tag.trigger) {
                is TriggerUiModel.BuiltIn -> TriggerPresentationModel.BuiltIn(
                    builtInTriggerUiToPresentationMapper.toPresentation(trigger.trigger)
                )

                is TriggerUiModel.Custom ->
                    TriggerPresentationModel.Custom(
                        customTagUiToPresentationMapper
                            .toPresentation(trigger.tag)
                    )
            }
        )
    }
}
