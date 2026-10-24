package com.migrely.app.medication.domain.model

sealed interface MedicationsResultDomainModel {
    /** Active medications, most recently used first, then by name (BRD D35). */
    data class Loaded(val medications: List<MedicationDomainModel>) : MedicationsResultDomainModel

    data object Failed : MedicationsResultDomainModel
}
