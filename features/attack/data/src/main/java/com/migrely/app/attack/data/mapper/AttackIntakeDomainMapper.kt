package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.datasource.medication.model.IntakeDataModel
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

class AttackIntakeDomainMapper {
    fun toDomain(intake: IntakeDataModel) = AttackIntakeDomainModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = intake.takenAt,
        zone = zoneOf(intake.takenZoneId)
    )

    // A zone id saved on another device may be unknown here; UTC keeps the instant correct.
    @Suppress("SwallowedException")
    private fun zoneOf(id: String): ZoneId = try {
        ZoneId.of(id)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }
}
