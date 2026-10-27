package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.medication.domain.model.AcuteIntakeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel

class AcuteIntakeDomainMapper(private val zoneDomainMapper: ZoneDomainMapper) {
    fun toDomain(intake: AcuteIntakeDataModel) = AcuteIntakeDomainModel(
        takenAt = intake.takenAt,
        zone = zoneDomainMapper.toDomain(intake.takenZoneId),
        category = when (intake.category) {
            MedicationCategoryDataModel.SimplePainkiller ->
                MedicationCategoryDomainModel.SimplePainkiller
            MedicationCategoryDataModel.Triptan -> MedicationCategoryDomainModel.Triptan
            MedicationCategoryDataModel.CombinationOrOther ->
                MedicationCategoryDomainModel.CombinationOrOther
            null -> null
        }
    )
}
