package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import java.time.Instant
import java.time.ZoneId

class AttackIntakeUiToPresentationMapper {
    fun toPresentation(intake: AttackIntakeUiModel) = AttackIntakePresentationModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = Instant.ofEpochMilli(intake.takenAtEpochMillis),
        zone = ZoneId.of(intake.zoneId)
    )
}
