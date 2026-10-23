package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel

interface UpdateLongAttackCheckRepository {
    fun updateLongAttackCheck(
        attackId: String,
        check: LongAttackCheckDomainModel
    ): WriteResultDomainModel
}
