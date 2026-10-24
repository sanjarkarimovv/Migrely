package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.ReopenResultDomainModel

interface ReopenAttackRepository {
    /** Must be atomic: refuses when another attack is in progress (BRD EC-ATK-01). */
    fun reopenAttack(attackId: String): ReopenResultDomainModel
}
