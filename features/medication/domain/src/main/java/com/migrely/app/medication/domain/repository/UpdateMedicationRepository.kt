package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel

interface UpdateMedicationRepository {
    /** Replaces every field of [medicationId] and makes it active. */
    @Suppress("LongParameterList")
    fun updateMedication(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ): MedicationWriteResultDomainModel
}
