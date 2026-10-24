package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import java.time.Instant
import java.time.ZoneId

interface AddIntakeRepository {
    fun addIntake(
        medicationId: String,
        attackId: String?,
        takenAt: Instant,
        zone: ZoneId,
        dose: String?
    ): AddIntakeResultDomainModel
}
