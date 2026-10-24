package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel

class TagKindDataMapper {
    fun toData(kind: TagKindDomainModel): CustomTagKindDataModel = when (kind) {
        TagKindDomainModel.Symptom -> CustomTagKindDataModel.Symptom
        TagKindDomainModel.Trigger -> CustomTagKindDataModel.Trigger
    }
}
