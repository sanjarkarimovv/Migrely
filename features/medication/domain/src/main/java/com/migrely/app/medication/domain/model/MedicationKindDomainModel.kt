package com.migrely.app.medication.domain.model

/** Acute medications stop an attack and count towards overuse later; preventive ones are taken regularly (MED-F1). */
enum class MedicationKindDomainModel {
    Acute,
    Preventive
}
