package com.migrely.app.datasource.tag.datasource

import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.tag.dao.TagDao
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val NOW: Instant = Instant.parse("2026-10-23T09:00:00Z")

class TagDataSourceImplTest {
    private lateinit var classUnderTest: TagDataSourceImpl
    private val tagDao: TagDao = mock()

    @Before
    fun setUp() {
        classUnderTest = TagDataSourceImpl(
            tagDao = tagDao,
            customTagLocalMapper = CustomTagLocalMapper(),
            clock = Clock.fixed(NOW, ZoneOffset.UTC),
            generateId = { "new-id" }
        )
    }

    @Test
    fun `When creating a tag then inserts it unarchived and returns it`() {
        val actual = classUnderTest.createCustomTag(
            CustomTagKindDataModel.Symptom,
            "Jaw pain",
            "jaw pain"
        )

        verify(tagDao).insert(
            CustomTagLocalModel(
                "new-id",
                "SYMPTOM",
                "Jaw pain",
                "jaw pain",
                false,
                NOW.toEpochMilli()
            )
        )
        assertEquals(
            CustomTagDataModel("new-id", CustomTagKindDataModel.Symptom, "Jaw pain", false),
            actual
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a 41 character name when creating then a programming error`() {
        classUnderTest.createCustomTag(
            CustomTagKindDataModel.Symptom,
            "x".repeat(41),
            "x".repeat(41)
        )
    }

    @Test(expected = StorageFullDataException::class)
    fun `Given storage is full when creating then storage full`() {
        whenever(tagDao.insert(any())).doThrow(SQLiteFullException())

        classUnderTest.createCustomTag(CustomTagKindDataModel.Trigger, "Perfume", "perfume")
    }

    @Test
    fun `Given no ids when resolving tags then does not query`() {
        assertEquals(emptyList<CustomTagDataModel>(), classUnderTest.customTags(emptyList()))
        verify(tagDao, never()).byIds(any())
    }

    @Test
    fun `When listing active symptoms then maps the rows`() {
        given(tagDao.activeByKind("SYMPTOM"))
            .willReturn(
                listOf(CustomTagLocalModel("t", "SYMPTOM", "Jaw pain", "jaw pain", false, 1L))
            )

        assertEquals(
            listOf(CustomTagDataModel("t", CustomTagKindDataModel.Symptom, "Jaw pain", false)),
            classUnderTest.activeCustomTags(CustomTagKindDataModel.Symptom)
        )
    }
}
