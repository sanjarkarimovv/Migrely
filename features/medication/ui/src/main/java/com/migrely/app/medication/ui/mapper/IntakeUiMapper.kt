package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.IntakePresentationModel
import com.migrely.app.medication.ui.model.IntakeUiModel

class IntakeUiMapper {
    fun toUi(intake: IntakePresentationModel) = IntakeUiModel(
        id = intake.id,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAtEpochMillis = intake.takenAt.toEpochMilli(),
        zoneId = intake.zone.id,
        attackId = intake.attackId
    )
}
