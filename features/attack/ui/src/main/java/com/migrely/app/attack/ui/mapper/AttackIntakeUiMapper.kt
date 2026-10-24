package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel

class AttackIntakeUiMapper {
    fun toUi(intake: AttackIntakePresentationModel) = AttackIntakeUiModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAtEpochMillis = intake.takenAt.toEpochMilli(),
        zoneId = intake.zone.id
    )
}
