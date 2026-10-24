package com.migrely.app.medication.domain.model

sealed interface AcuteIntakesResultDomainModel {
    data class Loaded(val intakes: List<AcuteIntakeDomainModel>) : AcuteIntakesResultDomainModel

    data object Failed : AcuteIntakesResultDomainModel
}
