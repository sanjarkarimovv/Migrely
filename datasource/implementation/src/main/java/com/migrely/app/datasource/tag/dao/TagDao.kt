package com.migrely.app.datasource.tag.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.migrely.app.datasource.tag.model.CustomTagLocalModel

@Dao
interface TagDao {
    @Query(
        "SELECT * FROM custom_tag WHERE kind = :kind AND archived = 0 ORDER BY name COLLATE NOCASE"
    )
    fun activeByKind(kind: String): List<CustomTagLocalModel>

    @Query("SELECT * FROM custom_tag WHERE id IN (:ids)")
    fun byIds(ids: List<String>): List<CustomTagLocalModel>

    @Query("SELECT * FROM custom_tag WHERE kind = :kind AND name_key = :nameKey LIMIT 1")
    fun byNameKey(kind: String, nameKey: String): CustomTagLocalModel?

    @Query("SELECT COUNT(*) FROM custom_tag WHERE kind = :kind AND archived = 0")
    fun countActive(kind: String): Int

    @Insert
    fun insert(tag: CustomTagLocalModel)

    @Query("UPDATE custom_tag SET archived = :archived WHERE id = :tagId")
    fun setArchived(tagId: String, archived: Boolean)
}
