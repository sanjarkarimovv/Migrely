package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel

interface GetAttackRepository {
    fun attack(attackId: String): AttackLookupResultDomainModel
}
