package com.migrely.app.attack.presentation.model

sealed interface SymptomPresentationModel {
    data class BuiltIn(val symptom: BuiltInSymptomPresentationModel) : SymptomPresentationModel

    data class Custom(val tag: CustomTagPresentationModel) : SymptomPresentationModel
}
