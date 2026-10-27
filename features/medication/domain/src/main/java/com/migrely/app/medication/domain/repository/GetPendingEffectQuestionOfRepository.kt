package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import java.time.Instant

interface GetPendingEffectQuestionOfRepository {
    /** [intakeId]'s question while it is open and was taken in the window from [takenFrom] to [takenTo]. */
    fun pendingEffectQuestionOf(
        intakeId: String,
        takenFrom: Instant,
        takenTo: Instant
    ): PendingEffectQuestionResultDomainModel
}
