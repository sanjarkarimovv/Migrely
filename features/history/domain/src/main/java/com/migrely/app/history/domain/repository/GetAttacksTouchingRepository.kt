package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import java.time.Instant

interface GetAttacksTouchingRepository {
    /** Every attack, ended or in progress, that overlaps `[from, to)`, oldest first. */
    fun attacksTouching(from: Instant, to: Instant): AttacksTouchingResultDomainModel
}
