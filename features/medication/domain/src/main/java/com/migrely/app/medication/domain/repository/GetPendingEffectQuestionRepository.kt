package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import java.time.Instant

interface GetPendingEffectQuestionRepository {
    /** Taken in the half-open window [[takenFrom], [takenTo]). */
    fun pendingEffectQuestion(
        takenFrom: Instant,
        takenTo: Instant
    ): PendingEffectQuestionResultDomainModel
}
