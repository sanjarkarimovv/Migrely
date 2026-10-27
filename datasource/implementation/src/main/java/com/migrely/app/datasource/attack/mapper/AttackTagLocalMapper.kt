package com.migrely.app.datasource.attack.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel

private const val LOCATION = "LOCATION"
private const val SYMPTOM = "SYMPTOM"
private const val TRIGGER = "TRIGGER"
private const val CUSTOM_PREFIX = AttackTagLocalModel.CUSTOM_KEY_PREFIX

/** Turns stored `attack_tag` rows into tags and back. Keys this version does not know are skipped. */
class AttackTagLocalMapper {
    fun toData(local: AttackTagLocalModel): AttackTagDataModel? {
        val customId = local.tagKey.removePrefix(CUSTOM_PREFIX).takeIf {
            local.tagKey.startsWith(CUSTOM_PREFIX)
        }
        return when (local.kind) {
            LOCATION -> HeadLocationDataModel.entries.firstOrNull { it.key == local.tagKey }
                ?.let(AttackTagDataModel::Location)

            SYMPTOM -> customId?.let(AttackTagDataModel::CustomSymptom)
                ?: BuiltInSymptomDataModel.entries.firstOrNull { it.key == local.tagKey }
                    ?.let(AttackTagDataModel::BuiltInSymptom)

            TRIGGER -> customId?.let(AttackTagDataModel::CustomTrigger)
                ?: BuiltInTriggerDataModel.entries.firstOrNull { it.key == local.tagKey }
                    ?.let(AttackTagDataModel::BuiltInTrigger)

            else -> null
        }
    }

    fun toLocal(attackId: String, tag: AttackTagDataModel): AttackTagLocalModel = when (tag) {
        is AttackTagDataModel.Location -> AttackTagLocalModel(attackId, LOCATION, tag.location.key)
        is AttackTagDataModel.BuiltInSymptom -> AttackTagLocalModel(
            attackId,
            SYMPTOM,
            tag.symptom.key
        )
        is AttackTagDataModel.BuiltInTrigger -> AttackTagLocalModel(
            attackId,
            TRIGGER,
            tag.trigger.key
        )
        is AttackTagDataModel.CustomSymptom -> AttackTagLocalModel(
            attackId,
            SYMPTOM,
            CUSTOM_PREFIX + tag.tagId
        )
        is AttackTagDataModel.CustomTrigger -> AttackTagLocalModel(
            attackId,
            TRIGGER,
            CUSTOM_PREFIX + tag.tagId
        )
    }
}
