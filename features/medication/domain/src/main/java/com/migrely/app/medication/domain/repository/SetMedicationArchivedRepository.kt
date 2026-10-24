package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.WriteResultDomainModel

interface SetMedicationArchivedRepository {
    fun setMedicationArchived(medicationId: String, archived: Boolean): WriteResultDomainModel
}
