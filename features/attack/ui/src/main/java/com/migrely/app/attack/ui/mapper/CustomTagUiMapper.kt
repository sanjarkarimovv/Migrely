package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.ui.model.CustomTagUiModel

class CustomTagUiMapper(private val tagKindUiMapper: TagKindUiMapper) {
    fun toUi(tag: CustomTagPresentationModel) = CustomTagUiModel(
        id = tag.id,
        kind = tagKindUiMapper.toUi(tag.kind),
        name = tag.name,
        archived = tag.archived
    )
}
