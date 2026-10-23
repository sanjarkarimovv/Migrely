package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface SelectAttackTagRepository {
    fun selectAttackTag(
        attackId: String,
        tag: AttackTagDomainModel,
        selected: Boolean
    ): WriteResultDomainModel
}
