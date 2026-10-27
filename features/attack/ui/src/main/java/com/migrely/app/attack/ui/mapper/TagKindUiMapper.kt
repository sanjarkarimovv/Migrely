package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.ui.model.TagKindUiModel

class TagKindUiMapper {
    fun toUi(tagKind: TagKindPresentationModel): TagKindUiModel = when (tagKind) {
        TagKindPresentationModel.Symptom -> TagKindUiModel.Symptom
        TagKindPresentationModel.Trigger -> TagKindUiModel.Trigger
    }
}
