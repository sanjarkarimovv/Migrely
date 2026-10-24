package com.migrely.app.medication.domain.repository

import java.time.Instant

interface ScheduleEffectQuestionRepository {
    /** Plans the "Did it help?" notification for intake [intakeId] at about [at] (BRD D67). */
    fun scheduleEffectQuestion(intakeId: String, at: Instant)
}
