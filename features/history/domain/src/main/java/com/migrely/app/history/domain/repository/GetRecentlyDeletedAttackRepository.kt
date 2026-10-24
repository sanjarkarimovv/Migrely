package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.DeletedAttackDomainModel

interface GetRecentlyDeletedAttackRepository {
    /** The attack deleted most recently while it can still be restored, or null, also when the lookup fails. */
    fun recentlyDeletedAttack(): DeletedAttackDomainModel?
}
