package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface SetCustomTagArchivedRepository {
    fun setCustomTagArchived(tagId: String, archived: Boolean): WriteResultDomainModel
}
