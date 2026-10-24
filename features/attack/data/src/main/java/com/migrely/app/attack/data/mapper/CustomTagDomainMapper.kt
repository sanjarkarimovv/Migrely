package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel

class CustomTagDomainMapper {
    fun toDomain(tag: CustomTagDataModel): CustomTagDomainModel = CustomTagDomainModel(
        id = tag.id,
        kind = when (tag.kind) {
            CustomTagKindDataModel.Symptom -> TagKindDomainModel.Symptom
            CustomTagKindDataModel.Trigger -> TagKindDomainModel.Trigger
        },
        name = tag.name,
        archived = tag.archived
    )
}
