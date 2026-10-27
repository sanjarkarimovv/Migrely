package com.migrely.app.medication.domain.model

sealed interface MedicationWriteResultDomainModel {
    data class Done(val medication: MedicationDomainModel) : MedicationWriteResultDomainModel

    data object StorageFull : MedicationWriteResultDomainModel

    data object Failed : MedicationWriteResultDomainModel
}
