package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.attack.presentation.model.AttackTagPresentationModel
import com.migrely.app.attack.presentation.model.SymptomPresentationModel
import com.migrely.app.attack.presentation.model.TriggerPresentationModel

class AttackTagDomainMapper(
    private val headLocationDomainMapper: HeadLocationDomainMapper,
    private val builtInSymptomDomainMapper: BuiltInSymptomDomainMapper,
    private val builtInTriggerDomainMapper: BuiltInTriggerDomainMapper,
    private val customTagDomainMapper: CustomTagDomainMapper
) {
    fun toDomain(tag: AttackTagPresentationModel): AttackTagDomainModel = when (tag) {
        is AttackTagPresentationModel.Location ->
            AttackTagDomainModel.Location(headLocationDomainMapper.toDomain(tag.location))

        is AttackTagPresentationModel.Symptom -> AttackTagDomainModel.Symptom(
            when (val symptom = tag.symptom) {
                is SymptomPresentationModel.BuiltIn ->
                    SymptomDomainModel.BuiltIn(builtInSymptomDomainMapper.toDomain(symptom.symptom))

                is SymptomPresentationModel.Custom -> SymptomDomainModel.Custom(
                    customTagDomainMapper
                        .toDomain(symptom.tag)
                )
            }
        )

        is AttackTagPresentationModel.Trigger -> AttackTagDomainModel.Trigger(
            when (val trigger = tag.trigger) {
                is TriggerPresentationModel.BuiltIn ->
                    TriggerDomainModel.BuiltIn(builtInTriggerDomainMapper.toDomain(trigger.trigger))

                is TriggerPresentationModel.Custom -> TriggerDomainModel.Custom(
                    customTagDomainMapper
                        .toDomain(trigger.tag)
                )
            }
        )
    }
}
