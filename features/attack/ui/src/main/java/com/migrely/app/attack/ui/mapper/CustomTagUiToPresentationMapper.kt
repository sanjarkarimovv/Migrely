package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.ui.model.CustomTagUiModel

class CustomTagUiToPresentationMapper(
    private val tagKindUiToPresentationMapper: TagKindUiToPresentationMapper
) {
    fun toPresentation(tag: CustomTagUiModel) = CustomTagPresentationModel(
        id = tag.id,
        kind = tagKindUiToPresentationMapper.toPresentation(tag.kind),
        name = tag.name,
        archived = tag.archived
    )
}
