package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel

interface GetAttackDetailsRepository {
    fun attackDetails(attackId: String): AttackDetailsLookupResultDomainModel
}
