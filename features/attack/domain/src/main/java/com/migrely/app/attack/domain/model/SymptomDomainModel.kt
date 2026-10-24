package com.migrely.app.attack.domain.model

sealed interface SymptomDomainModel {
    data class BuiltIn(val symptom: BuiltInSymptomDomainModel) : SymptomDomainModel

    data class Custom(val tag: CustomTagDomainModel) : SymptomDomainModel
}
