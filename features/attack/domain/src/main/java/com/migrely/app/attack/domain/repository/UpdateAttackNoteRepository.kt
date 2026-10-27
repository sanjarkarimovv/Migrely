package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface UpdateAttackNoteRepository {
    fun updateAttackNote(attackId: String, note: String): WriteResultDomainModel
}
