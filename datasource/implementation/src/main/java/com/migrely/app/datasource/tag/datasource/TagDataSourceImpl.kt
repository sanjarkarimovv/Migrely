package com.migrely.app.datasource.tag.datasource

import com.migrely.app.datasource.database.database
import com.migrely.app.datasource.tag.dao.TagDao
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import java.time.Clock
import java.util.UUID

private const val MAXIMUM_NAME_LENGTH = 40

class TagDataSourceImpl(
    private val tagDao: TagDao,
    private val customTagLocalMapper: CustomTagLocalMapper,
    private val clock: Clock = Clock.systemUTC(),
    private val generateId: () -> String = { UUID.randomUUID().toString() }
) : TagDataSource {
    override fun activeCustomTags(kind: CustomTagKindDataModel): List<CustomTagDataModel> =
        database {
            tagDao.activeByKind(
                customTagLocalMapper.toLocalKind(kind)
            ).mapNotNull(customTagLocalMapper::toData)
        }

    override fun customTags(ids: Collection<String>): List<CustomTagDataModel> =
        if (ids.isEmpty()) {
            emptyList()
        } else {
            database { tagDao.byIds(ids.toList()).mapNotNull(customTagLocalMapper::toData) }
        }

    override fun customTagByNameKey(
        kind: CustomTagKindDataModel,
        nameKey: String
    ): CustomTagDataModel? = database {
        tagDao.byNameKey(
            customTagLocalMapper.toLocalKind(kind),
            nameKey
        )?.let(customTagLocalMapper::toData)
    }

    override fun activeCustomTagCount(kind: CustomTagKindDataModel): Int = database {
        tagDao.countActive(customTagLocalMapper.toLocalKind(kind))
    }

    override fun createCustomTag(
        kind: CustomTagKindDataModel,
        name: String,
        nameKey: String
    ): CustomTagDataModel {
        // Names outside 1..40 characters are a programming error (BRD EC-ATK-12), not a storage failure.
        require(name.isNotBlank() && name.length <= MAXIMUM_NAME_LENGTH) {
            "Invalid tag name length ${name.length}"
        }
        val local = CustomTagLocalModel(
            id = generateId(),
            kind = customTagLocalMapper.toLocalKind(kind),
            name = name,
            nameKey = nameKey,
            archived = false,
            createdEpochMillis = clock.millis()
        )
        database { tagDao.insert(local) }
        return CustomTagDataModel(id = local.id, kind = kind, name = name, archived = false)
    }

    override fun setArchived(tagId: String, archived: Boolean) = database {
        tagDao.setArchived(tagId, archived)
    }
}
