package com.migrely.app.datasource.backup.model

import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import java.time.Instant

data class BackupMedicationDataModel(
    val id: String,
    val name: String,
    val kind: MedicationKindDataModel,
    val category: MedicationCategoryDataModel?,
    val defaultDose: String?,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
