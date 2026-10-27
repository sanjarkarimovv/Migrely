package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.WriteResultDomainModel
import java.time.Instant

interface UpdateAttackStartRepository {
    fun updateAttackStart(attackId: String, start: Instant): WriteResultDomainModel
}
