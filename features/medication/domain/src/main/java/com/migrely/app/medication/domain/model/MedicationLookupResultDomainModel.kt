package com.migrely.app.medication.domain.model

sealed interface MedicationLookupResultDomainModel {
    data class Found(val medication: MedicationDomainModel) : MedicationLookupResultDomainModel

    data object NotFound : MedicationLookupResultDomainModel

    data object Failed : MedicationLookupResultDomainModel
}
