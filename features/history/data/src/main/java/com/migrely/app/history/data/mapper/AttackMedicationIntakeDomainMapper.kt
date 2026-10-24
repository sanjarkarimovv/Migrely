package com.migrely.app.history.data.mapper

import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.history.domain.model.AttackMedicationIntakeDomainModel

class AttackMedicationIntakeDomainMapper(private val zoneDomainMapper: ZoneDomainMapper) {
    fun toDomain(intake: AcuteIntakeDataModel): AttackMedicationIntakeDomainModel =
        AttackMedicationIntakeDomainModel(
            intake.takenAt,
            zoneDomainMapper.toDomain(intake.takenZoneId)
        )
}
