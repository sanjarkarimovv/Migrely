package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.MedicationLogDomainModel
import com.migrely.app.medication.presentation.model.MedicationLogPresentationModel

class MedicationLogPresentationMapper(
    private val medicationPresentationMapper: MedicationPresentationMapper
) {
    fun toPresentation(log: MedicationLogDomainModel) = MedicationLogPresentationModel(
        defaultTime = log.defaultTime,
        zone = log.zone,
        attackId = log.attackId,
        medications = log.medications.map(medicationPresentationMapper::toPresentation)
    )
}
