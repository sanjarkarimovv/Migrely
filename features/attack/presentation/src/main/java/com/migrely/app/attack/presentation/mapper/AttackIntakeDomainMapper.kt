package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel

/** Turns a removed intake back into its domain form for "Undo". */
class AttackIntakeDomainMapper {
    fun toDomain(intake: AttackIntakePresentationModel) = AttackIntakeDomainModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = intake.takenAt,
        zone = intake.zone
    )
}
