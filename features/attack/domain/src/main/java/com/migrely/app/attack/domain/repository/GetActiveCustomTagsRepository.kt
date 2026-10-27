package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.CustomTagsResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel

interface GetActiveCustomTagsRepository {
    fun activeCustomTags(kind: TagKindDomainModel): CustomTagsResultDomainModel
}
