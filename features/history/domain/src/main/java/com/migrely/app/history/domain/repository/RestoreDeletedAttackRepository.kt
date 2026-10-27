package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.UndoResultDomainModel

interface RestoreDeletedAttackRepository {
    fun restoreDeletedAttack(attackId: String): UndoResultDomainModel
}
