package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.AttackMergeDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface MergeAttacksRepository {
    /** Writes [merge] in one step (BRD D27). */
    fun mergeAttacks(merge: AttackMergeDomainModel): WriteResultDomainModel
}
