package com.migrely.app.medication.domain.model

/** [id] is null for a new medication. Text is taken as typed; the use case trims and checks it. */
data class SaveMedicationRequestDomainModel(
    val id: String?,
    val name: String,
    val kind: MedicationKindDomainModel,
    val category: MedicationCategoryDomainModel?,
    val defaultDose: String?
)
