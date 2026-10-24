package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface RestoreIntakeRepository {
    /** Stores [intake] again with its id, for the attack [attackId]. */
    fun restoreIntake(attackId: String, intake: AttackIntakeDomainModel): WriteResultDomainModel
}
