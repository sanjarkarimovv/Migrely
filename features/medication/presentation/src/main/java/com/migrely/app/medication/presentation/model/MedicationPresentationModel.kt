package com.migrely.app.medication.presentation.model

/** [category] is null for preventive medications and for "Not sure". */
data class MedicationPresentationModel(
    val id: String,
    val name: String,
    val kind: MedicationKindPresentationModel,
    val category: MedicationCategoryPresentationModel?,
    val defaultDose: String?
)
