package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.MedicationsResultDomainModel

interface GetActiveMedicationsRepository {
    /** Active medications, most recently used first, then by name. */
    fun activeMedications(): MedicationsResultDomainModel
}
