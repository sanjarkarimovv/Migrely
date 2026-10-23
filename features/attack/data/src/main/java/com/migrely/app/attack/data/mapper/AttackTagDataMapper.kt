package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel

class AttackTagDataMapper(
    private val headLocationDataMapper: HeadLocationDataMapper,
    private val builtInSymptomDataMapper: BuiltInSymptomDataMapper,
    private val builtInTriggerDataMapper: BuiltInTriggerDataMapper
) {
    fun toData(tag: AttackTagDomainModel): AttackTagDataModel = when (tag) {
        is AttackTagDomainModel.Location -> AttackTagDataModel.Location(
            headLocationDataMapper.toData(tag.location)
        )
        is AttackTagDomainModel.Symptom -> when (val symptom = tag.symptom) {
            is SymptomDomainModel.BuiltIn ->
                AttackTagDataModel.BuiltInSymptom(builtInSymptomDataMapper.toData(symptom.symptom))

            is SymptomDomainModel.Custom -> AttackTagDataModel.CustomSymptom(symptom.tag.id)
        }

        is AttackTagDomainModel.Trigger -> when (val trigger = tag.trigger) {
            is TriggerDomainModel.BuiltIn ->
                AttackTagDataModel.BuiltInTrigger(builtInTriggerDataMapper.toData(trigger.trigger))

            is TriggerDomainModel.Custom -> AttackTagDataModel.CustomTrigger(trigger.tag.id)
        }
    }
}
