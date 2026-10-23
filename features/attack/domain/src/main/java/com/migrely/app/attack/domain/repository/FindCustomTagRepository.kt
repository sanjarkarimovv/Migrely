package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.CustomTagLookupResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel

interface FindCustomTagRepository {
    /** The tag of [kind] whose trimmed, lower-cased name equals [nameKey], archived or not. */
    fun customTag(kind: TagKindDomainModel, nameKey: String): CustomTagLookupResultDomainModel
}
