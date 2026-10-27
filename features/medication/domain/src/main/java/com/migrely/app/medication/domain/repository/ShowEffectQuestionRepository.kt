package com.migrely.app.medication.domain.repository

interface ShowEffectQuestionRepository {
    fun showEffectQuestion(intakeId: String, medicationName: String)
}
