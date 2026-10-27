package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.IntakeDomainModel
import com.migrely.app.medication.presentation.model.IntakePresentationModel

class IntakePresentationMapper {
    fun toPresentation(intake: IntakeDomainModel) = IntakePresentationModel(
        id = intake.id,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = intake.takenAt,
        zone = intake.zone,
        attackId = intake.attackId
    )
}
