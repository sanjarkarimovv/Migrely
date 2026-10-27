package com.migrely.app.datasource.medication.model

/** [category] is null for preventive medications and for "Not sure". */
data class MedicationDataModel(
    val id: String,
    val name: String,
    val kind: MedicationKindDataModel,
    val category: MedicationCategoryDataModel?,
    val defaultDose: String?,
    val archived: Boolean
)
