package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.WriteResultDomainModel
import java.time.Instant

interface EndAttackRepository {
    fun endAttack(attackId: String, end: Instant): WriteResultDomainModel
}
