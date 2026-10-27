package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel

class TagKindDomainMapper {
    fun toDomain(tagKind: TagKindPresentationModel): TagKindDomainModel = when (tagKind) {
        TagKindPresentationModel.Symptom -> TagKindDomainModel.Symptom
        TagKindPresentationModel.Trigger -> TagKindDomainModel.Trigger
    }
}
