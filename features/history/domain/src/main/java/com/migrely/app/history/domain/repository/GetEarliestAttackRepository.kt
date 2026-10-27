package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel

interface GetEarliestAttackRepository {
    fun earliestAttack(): EarliestAttackResultDomainModel
}
