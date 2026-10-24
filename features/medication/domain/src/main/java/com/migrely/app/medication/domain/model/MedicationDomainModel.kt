package com.migrely.app.medication.domain.model

/** One of the user's medications. [category] is null for preventive ones and for "Not sure" (BRD D35). */
data class MedicationDomainModel(
    val id: String,
    val name: String,
    val kind: MedicationKindDomainModel,
    val category: MedicationCategoryDomainModel?,
    val defaultDose: String?,
    val archived: Boolean
)
