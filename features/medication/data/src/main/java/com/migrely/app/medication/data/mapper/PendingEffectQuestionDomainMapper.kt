package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.PendingEffectQuestionDataModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel

class PendingEffectQuestionDomainMapper(private val zoneDomainMapper: ZoneDomainMapper) {
    fun toDomain(question: PendingEffectQuestionDataModel) = PendingEffectQuestionDomainModel(
        intakeId = question.intakeId,
        medicationName = question.medicationName,
        dose = question.dose,
        takenAt = question.takenAt,
        zone = zoneDomainMapper.toDomain(question.takenZoneId)
    )
}
