package com.migrely.app.medication.domain.repository

interface CancelEffectQuestionRepository {
    /** Drops intake [intakeId]'s planned "Did it help?" notification, and the notification if it shows. */
    fun cancelEffectQuestion(intakeId: String)
}
