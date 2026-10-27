package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel

class CustomTagDomainMapper(private val tagKindDomainMapper: TagKindDomainMapper) {
    fun toDomain(tag: CustomTagPresentationModel) = CustomTagDomainModel(
        id = tag.id,
        kind = tagKindDomainMapper.toDomain(tag.kind),
        name = tag.name,
        archived = tag.archived
    )
}
