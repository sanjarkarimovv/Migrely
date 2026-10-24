package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel

interface FindMedicationByNameRepository {
    /** The medication, archived ones included, whose trimmed lower-case name is [nameKey]. */
    fun medicationByNameKey(nameKey: String): MedicationLookupResultDomainModel
}
