package com.migrely.app.attack.domain.model

/** One selectable detail of an attack: where it hurt, a symptom or a trigger. */
sealed interface AttackTagDomainModel {
    data class Location(val location: HeadLocationDomainModel) : AttackTagDomainModel

    data class Symptom(val symptom: SymptomDomainModel) : AttackTagDomainModel

    data class Trigger(val trigger: TriggerDomainModel) : AttackTagDomainModel
}
