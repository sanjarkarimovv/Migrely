package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface DeleteAttackRepository {
    /** Hides the attack; it can be restored during the undo window and is purged after it (BRD D23). */
    fun deleteAttack(attackId: String): WriteResultDomainModel
}
