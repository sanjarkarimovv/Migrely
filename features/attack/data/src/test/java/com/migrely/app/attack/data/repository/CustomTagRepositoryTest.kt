package com.migrely.app.attack.data.repository

import com.migrely.app.attack.data.mapper.CustomTagDomainMapper
import com.migrely.app.attack.data.mapper.TagKindDataMapper
import com.migrely.app.attack.domain.model.CreateCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.CustomTagLookupResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagsResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.tag.datasource.TagDataSource
import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CustomTagRepositoryTest {
    private val tagDataSource: TagDataSource = mock()
    private val classUnderTest = CustomTagRepository(
        tagDataSource,
        CustomTagDomainMapper(),
        TagKindDataMapper()
    )
    private val dataTag = CustomTagDataModel("t", CustomTagKindDataModel.Trigger, "Perfume", false)
    private val domainTag = CustomTagDomainModel("t", TagKindDomainModel.Trigger, "Perfume", false)

    @Test
    fun `When listing active triggers then maps them`() {
        given(tagDataSource.activeCustomTags(CustomTagKindDataModel.Trigger))
            .willReturn(listOf(dataTag))

        assertEquals(
            CustomTagsResultDomainModel.Loaded(listOf(domainTag)),
            classUnderTest.activeCustomTags(TagKindDomainModel.Trigger)
        )
    }

    @Test
    fun `Given the database fails when listing then failed`() {
        whenever(tagDataSource.activeCustomTags(any())).doThrow(
            DatabaseDataException(RuntimeException())
        )

        assertEquals(
            CustomTagsResultDomainModel.Failed,
            classUnderTest.activeCustomTags(TagKindDomainModel.Symptom)
        )
    }

    @Test
    fun `Given no match when looking up a name then none`() {
        assertEquals(
            CustomTagLookupResultDomainModel.None,
            classUnderTest.customTag(TagKindDomainModel.Trigger, "perfume")
        )
    }

    @Test
    fun `Given a match when looking up a name then found`() {
        given(tagDataSource.customTagByNameKey(CustomTagKindDataModel.Trigger, "perfume"))
            .willReturn(dataTag)

        assertEquals(
            CustomTagLookupResultDomainModel.Found(domainTag),
            classUnderTest.customTag(TagKindDomainModel.Trigger, "perfume")
        )
    }

    @Test
    fun `When counting then returns the count`() {
        given(tagDataSource.activeCustomTagCount(CustomTagKindDataModel.Symptom)).willReturn(100)

        assertEquals(
            CustomTagCountResultDomainModel.Counted(100),
            classUnderTest.activeCustomTagCount(TagKindDomainModel.Symptom)
        )
    }

    @Test
    fun `When creating then returns the created tag`() {
        given(tagDataSource.createCustomTag(CustomTagKindDataModel.Trigger, "Perfume", "perfume"))
            .willReturn(dataTag)

        assertEquals(
            CreateCustomTagResultDomainModel.Created(domainTag),
            classUnderTest.createCustomTag(TagKindDomainModel.Trigger, "Perfume", "perfume")
        )
    }

    @Test
    fun `Given storage is full when creating then storage full`() {
        whenever(tagDataSource.createCustomTag(any(), any(), any())).doThrow(
            StorageFullDataException(RuntimeException())
        )

        assertEquals(
            CreateCustomTagResultDomainModel.StorageFull,
            classUnderTest.createCustomTag(TagKindDomainModel.Trigger, "Perfume", "perfume")
        )
    }

    @Test
    fun `Given the database fails when archiving then failed`() {
        whenever(tagDataSource.setArchived(any(), any())).doThrow(
            DatabaseDataException(RuntimeException())
        )

        assertEquals(WriteResultDomainModel.Failed, classUnderTest.setCustomTagArchived("t", true))
    }
}
