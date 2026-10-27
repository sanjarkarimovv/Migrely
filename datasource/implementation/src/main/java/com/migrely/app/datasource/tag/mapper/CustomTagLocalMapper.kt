package com.migrely.app.datasource.tag.mapper

import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel

private const val SYMPTOM = "SYMPTOM"
private const val TRIGGER = "TRIGGER"

class CustomTagLocalMapper {
    /** Null for a kind this version does not know, so a newer database never crashes an older app. */
    fun toData(local: CustomTagLocalModel): CustomTagDataModel? = kindOf(local.kind)?.let { kind ->
        CustomTagDataModel(id = local.id, kind = kind, name = local.name, archived = local.archived)
    }

    fun toLocalKind(kind: CustomTagKindDataModel): String = when (kind) {
        CustomTagKindDataModel.Symptom -> SYMPTOM
        CustomTagKindDataModel.Trigger -> TRIGGER
    }

    private fun kindOf(stored: String): CustomTagKindDataModel? = when (stored) {
        SYMPTOM -> CustomTagKindDataModel.Symptom
        TRIGGER -> CustomTagKindDataModel.Trigger
        else -> null
    }
}
