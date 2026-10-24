package com.migrely.app.datasource.tag.datasource

import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel

/**
 * The user's own symptoms and triggers. Tags are never deleted, only archived, so attacks can always show them (BRD
 * EC-ATK-11). Every function blocks and throws the storage exceptions described on
 * [com.migrely.app.datasource.attack.datasource.AttackDataSource].
 */
interface TagDataSource {
    fun activeCustomTags(kind: CustomTagKindDataModel): List<CustomTagDataModel>

    fun customTags(ids: Collection<String>): List<CustomTagDataModel>

    /** The tag whose name, trimmed and lower-cased, is [nameKey]; archived tags included. */
    fun customTagByNameKey(kind: CustomTagKindDataModel, nameKey: String): CustomTagDataModel?

    fun activeCustomTagCount(kind: CustomTagKindDataModel): Int

    fun createCustomTag(
        kind: CustomTagKindDataModel,
        name: String,
        nameKey: String
    ): CustomTagDataModel

    fun setArchived(tagId: String, archived: Boolean)
}
