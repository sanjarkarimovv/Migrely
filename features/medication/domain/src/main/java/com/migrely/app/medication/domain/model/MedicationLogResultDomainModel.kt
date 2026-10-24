package com.migrely.app.medication.domain.model

sealed interface MedicationLogResultDomainModel {
    data class Loaded(val log: MedicationLogDomainModel) : MedicationLogResultDomainModel

    data object Failed : MedicationLogResultDomainModel
}
