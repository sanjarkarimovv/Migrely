package com.migrely.app.attack.presentation.model

sealed interface TriggerPresentationModel {
    data class BuiltIn(val trigger: BuiltInTriggerPresentationModel) : TriggerPresentationModel

    data class Custom(val tag: CustomTagPresentationModel) : TriggerPresentationModel
}
