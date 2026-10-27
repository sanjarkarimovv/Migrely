package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.ui.model.TagKindUiModel

class TagKindUiToPresentationMapper {
    fun toPresentation(tagKind: TagKindUiModel): TagKindPresentationModel = when (tagKind) {
        TagKindUiModel.Symptom -> TagKindPresentationModel.Symptom
        TagKindUiModel.Trigger -> TagKindPresentationModel.Trigger
    }
}
