package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel

class CustomTagPresentationMapper(
    private val tagKindPresentationMapper: TagKindPresentationMapper
) {
    fun toPresentation(tag: CustomTagDomainModel) = CustomTagPresentationModel(
        id = tag.id,
        kind = tagKindPresentationMapper.toPresentation(tag.kind),
        name = tag.name,
        archived = tag.archived
    )
}
