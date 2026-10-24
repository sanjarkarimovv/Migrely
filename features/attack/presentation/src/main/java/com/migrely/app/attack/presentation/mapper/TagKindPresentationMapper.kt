package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel

class TagKindPresentationMapper {
    fun toPresentation(tagKind: TagKindDomainModel): TagKindPresentationModel = when (tagKind) {
        TagKindDomainModel.Symptom -> TagKindPresentationModel.Symptom
        TagKindDomainModel.Trigger -> TagKindPresentationModel.Trigger
    }
}
