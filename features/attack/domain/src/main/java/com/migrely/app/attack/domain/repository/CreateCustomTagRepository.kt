package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.CreateCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel

interface CreateCustomTagRepository {
    fun createCustomTag(
        kind: TagKindDomainModel,
        name: String,
        nameKey: String
    ): CreateCustomTagResultDomainModel
}
