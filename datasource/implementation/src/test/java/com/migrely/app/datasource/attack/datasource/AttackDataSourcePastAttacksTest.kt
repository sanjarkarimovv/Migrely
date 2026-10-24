package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.CreateEndedDataModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val NOW: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val START: Instant = NOW.minusSeconds(7_200)
private val END: Instant = NOW.minusSeconds(3_600)

class AttackDataSourcePastAttacksTest {
    private lateinit var classUnderTest: AttackDataSourceImpl
    private val attackDao: AttackDao = mock()

    @Before
    fun setUp() {
        classUnderTest = AttackDataSourceImpl(
            attackDao = attackDao,
            attackLocalMapper = AttackLocalMapper(),
            clock = Clock.fixed(NOW, ZoneOffset.UTC),
            generateId = { "new" }
        )
    }

    private fun local(id: String, start: Instant, end: Instant?) = AttackLocalModel(
        id = id,
        startEpochMillis = start.toEpochMilli(),
        startZoneId = "Asia/Tashkent",
        endEpochMillis = end?.toEpochMilli(),
        longCheckConfirmedEpochMillis = null,
        longCheckDisabled = false,
        createdEpochMillis = 1L,
        updatedEpochMillis = 1L
    )

    @Test
    fun `When looking for overlaps then maps every attack in the dao's order`() {
        given(attackDao.overlappingAll(null, START.toEpochMilli(), END.toEpochMilli()))
            .willReturn(
                listOf(
                    AttackWithPointsLocalModel(local("b", START, END), emptyList()),
                    AttackWithPointsLocalModel(local("c", END, null), emptyList())
                )
            )

        val actual = classUnderTest.overlapping(null, START, END)

        assertEquals(listOf("b", "c"), actual.map { it.id })
        assertEquals(null, actual[1].end)
    }

    @Test
    fun `Given nothing overlaps when creating an ended attack then it is inserted`() {
        given(attackDao.insertEndedIfNoOverlap(any())).willReturn(true)

        val actual = classUnderTest.createEnded(START, "Asia/Tashkent", END)

        assertEquals(CreateEndedDataModel.Created("new"), actual)
        verify(attackDao).insertEndedIfNoOverlap(
            local("new", START, END).copy(
                createdEpochMillis = NOW.toEpochMilli(),
                updatedEpochMillis = NOW.toEpochMilli()
            )
        )
    }

    @Test
    fun `Given the dao finds an overlap when creating an ended attack then overlapping`() {
        given(attackDao.insertEndedIfNoOverlap(any())).willReturn(false)

        assertEquals(
            CreateEndedDataModel.Overlapping,
            classUnderTest.createEnded(START, "Asia/Tashkent", END)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given an end at the start when creating an ended attack then a programming error`() {
        classUnderTest.createEnded(START, "Asia/Tashkent", START)
    }

    @Test(expected = StorageFullDataException::class)
    fun `Given the disk is full when creating an ended attack then storage full`() {
        whenever(attackDao.insertEndedIfNoOverlap(any())).doThrow(SQLiteFullException())

        classUnderTest.createEnded(START, "Asia/Tashkent", END)
    }

    @Test
    fun `When merging then passes every field to one dao transaction`() {
        classUnderTest.merge("a", listOf("b", "c"), START, "Asia/Tokyo", null, 3, "Note")

        verify(attackDao).mergeInto(
            survivorId = "a",
            absorbedIds = listOf("b", "c"),
            startEpochMillis = START.toEpochMilli(),
            startZoneId = "Asia/Tokyo",
            endEpochMillis = null,
            impact = 3,
            note = "Note",
            nowEpochMillis = NOW.toEpochMilli()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given the survivor is also absorbed when merging then a programming error`() {
        classUnderTest.merge("a", listOf("a"), START, "UTC", END, null, "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given an impact of 4 when merging then a programming error`() {
        classUnderTest.merge("a", listOf("b"), START, "UTC", END, 4, "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a note of 1001 characters when merging then a programming error`() {
        classUnderTest.merge("a", listOf("b"), START, "UTC", END, null, "n".repeat(1_001))
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given the database fails when merging then a database failure`() {
        whenever(
            attackDao.mergeInto(
                any(),
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any()
            )
        ).doThrow(SQLiteException())

        classUnderTest.merge("a", listOf("b"), START, "UTC", END, null, "")
    }
}
