package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.UndoResultDomainModel

interface RestoreDeletedAttackRepository {
    fun restoreDeletedAttack(attackId: String): UndoResultDomainModel
}
