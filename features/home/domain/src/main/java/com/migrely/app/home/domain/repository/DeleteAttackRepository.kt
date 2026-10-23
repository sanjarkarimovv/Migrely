package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.WriteResultDomainModel

interface DeleteAttackRepository {
    fun deleteAttack(attackId: String): WriteResultDomainModel
}
