package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel

interface CountActiveCustomTagsRepository {
    fun activeCustomTagCount(kind: TagKindDomainModel): CustomTagCountResultDomainModel
}
