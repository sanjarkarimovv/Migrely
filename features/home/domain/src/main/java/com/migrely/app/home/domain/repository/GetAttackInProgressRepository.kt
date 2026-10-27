package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.AttackLookupResultDomainModel

interface GetAttackInProgressRepository {
    fun attackInProgress(): AttackLookupResultDomainModel
}
