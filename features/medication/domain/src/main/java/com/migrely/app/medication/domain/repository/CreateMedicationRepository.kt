package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel

interface CreateMedicationRepository {
    fun createMedication(
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ): MedicationWriteResultDomainModel
}
