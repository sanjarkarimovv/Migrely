package com.migrely.app.attack.data.repository

import com.migrely.app.attack.data.mapper.CustomTagDomainMapper
import com.migrely.app.attack.data.mapper.TagKindDataMapper
import com.migrely.app.attack.domain.model.CreateCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagLookupResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagsResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.CreateCustomTagRepository
import com.migrely.app.attack.domain.repository.FindCustomTagRepository
import com.migrely.app.attack.domain.repository.GetActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.tag.datasource.TagDataSource

/** The user's own symptoms and triggers. Failures come back as values (docs/ARCHITECTURE.md section 7). */
class CustomTagRepository(
    private val tagDataSource: TagDataSource,
    private val customTagDomainMapper: CustomTagDomainMapper,
    private val tagKindDataMapper: TagKindDataMapper
) : GetActiveCustomTagsRepository,
    FindCustomTagRepository,
    CountActiveCustomTagsRepository,
    CreateCustomTagRepository,
    SetCustomTagArchivedRepository {
    override fun activeCustomTags(kind: TagKindDomainModel): CustomTagsResultDomainModel = try {
        CustomTagsResultDomainModel.Loaded(
            tagDataSource.activeCustomTags(tagKindDataMapper.toData(kind)).map(
                customTagDomainMapper::toDomain
            )
        )
    } catch (_: DataException) {
        CustomTagsResultDomainModel.Failed
    }

    override fun customTag(
        kind: TagKindDomainModel,
        nameKey: String
    ): CustomTagLookupResultDomainModel = try {
        tagDataSource.customTagByNameKey(tagKindDataMapper.toData(kind), nameKey)
            ?.let { CustomTagLookupResultDomainModel.Found(customTagDomainMapper.toDomain(it)) }
            ?: CustomTagLookupResultDomainModel.None
    } catch (_: DataException) {
        CustomTagLookupResultDomainModel.Failed
    }

    override fun activeCustomTagCount(kind: TagKindDomainModel): CustomTagCountResultDomainModel =
        try {
            CustomTagCountResultDomainModel.Counted(
                tagDataSource.activeCustomTagCount(tagKindDataMapper.toData(kind))
            )
        } catch (_: DataException) {
            CustomTagCountResultDomainModel.Failed
        }

    override fun createCustomTag(
        kind: TagKindDomainModel,
        name: String,
        nameKey: String
    ): CreateCustomTagResultDomainModel = try {
        CreateCustomTagResultDomainModel.Created(
            customTagDomainMapper
                .toDomain(
                    tagDataSource.createCustomTag(tagKindDataMapper.toData(kind), name, nameKey)
                )
        )
    } catch (_: StorageFullDataException) {
        CreateCustomTagResultDomainModel.StorageFull
    } catch (_: DataException) {
        CreateCustomTagResultDomainModel.Failed
    }

    override fun setCustomTagArchived(tagId: String, archived: Boolean): WriteResultDomainModel =
        try {
            tagDataSource.setArchived(tagId, archived)
            WriteResultDomainModel.Written
        } catch (_: StorageFullDataException) {
            WriteResultDomainModel.StorageFull
        } catch (_: DataException) {
            WriteResultDomainModel.Failed
        }
}
