package com.migrely.app.attack.domain.model

sealed interface TriggerDomainModel {
    data class BuiltIn(val trigger: BuiltInTriggerDomainModel) : TriggerDomainModel

    data class Custom(val tag: CustomTagDomainModel) : TriggerDomainModel
}
