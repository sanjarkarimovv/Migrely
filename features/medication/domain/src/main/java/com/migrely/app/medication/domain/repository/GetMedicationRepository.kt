package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel

interface GetMedicationRepository {
    fun medication(medicationId: String): MedicationLookupResultDomainModel
}
