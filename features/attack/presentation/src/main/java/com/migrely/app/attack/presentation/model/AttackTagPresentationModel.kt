package com.migrely.app.attack.presentation.model

sealed interface AttackTagPresentationModel {
    data class Location(val location: HeadLocationPresentationModel) : AttackTagPresentationModel

    data class Symptom(val symptom: SymptomPresentationModel) : AttackTagPresentationModel

    data class Trigger(val trigger: TriggerPresentationModel) : AttackTagPresentationModel
}
