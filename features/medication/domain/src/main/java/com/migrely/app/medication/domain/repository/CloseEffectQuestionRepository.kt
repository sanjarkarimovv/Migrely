package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.WriteResultDomainModel

interface CloseEffectQuestionRepository {
    fun closeEffectQuestion(intakeId: String): WriteResultDomainModel
}
