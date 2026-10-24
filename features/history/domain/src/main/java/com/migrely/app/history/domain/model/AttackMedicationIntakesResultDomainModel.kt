package com.migrely.app.history.domain.model

sealed interface AttackMedicationIntakesResultDomainModel {
    data class Loaded(val intakes: List<AttackMedicationIntakeDomainModel>) :
        AttackMedicationIntakesResultDomainModel

    data object Failed : AttackMedicationIntakesResultDomainModel
}
