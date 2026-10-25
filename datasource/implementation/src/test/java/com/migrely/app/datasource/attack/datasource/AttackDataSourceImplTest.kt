package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

class AttackDataSourceImplTest {
    private lateinit var classUnderTest: AttackDataSourceImpl
    private val attackDao: AttackDao = mock()
    private val mapper = AttackLocalMapper()

    @Before
    fun setUp() {
        classUnderTest = AttackDataSourceImpl(
            attackDao = attackDao,
            attackLocalMapper = mapper,
            clock = Clock.fixed(NOW, ZoneOffset.UTC),
            generateId = { "new-id" }
        )
    }

    @Test
    fun `Given none in progress when starting then inserts a new attack and returns Started`() {
        // Given
        val expectedLocal = AttackLocalModel(
            id = "new-id",
            startEpochMillis = NOW.toEpochMilli(),
            startZoneId = "Asia/Tashkent",
            endEpochMillis = null,
            longCheckConfirmedEpochMillis = null,
            longCheckDisabled = false,
            createdEpochMillis = NOW.toEpochMilli(),
            updatedEpochMillis = NOW.toEpochMilli()
        )
        val created = AttackWithPointsLocalModel(expectedLocal, emptyList())
        given(
            attackDao.startIfNoneInProgress(expectedLocal)
        ).willReturn(AttackDao.StartOutcome(created, created = true))

        // When
        val actual = classUnderTest.startIfNoneInProgress(NOW, "Asia/Tashkent")

        // Then
        assertEquals(AttackStartDataModel.Started(mapper.toData(created)), actual)
    }

    @Test
    fun `Given one in progress when starting then returns AlreadyInProgress`() {
        // Given
        val existing = AttackWithPointsLocalModel(
            AttackLocalModel("old", 1L, "UTC", null, null, false, 1L, 1L),
            emptyList()
        )
        given(
            attackDao.startIfNoneInProgress(any())
        ).willReturn(AttackDao.StartOutcome(existing, created = false))

        // When
        val actual = classUnderTest.startIfNoneInProgress(NOW, "UTC")

        // Then
        assertEquals(AttackStartDataModel.AlreadyInProgress(mapper.toData(existing)), actual)
    }

    @Test
    fun `When adding an intensity point then inserts it with its time and value`() {
        // When
        classUnderTest.addIntensityPoint("a", NOW, 6)

        // Then
        verify(attackDao).insertIntensityPoint(
            IntensityPointLocalModel(attackId = "a", atEpochMillis = NOW.toEpochMilli(), value = 6)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given an intensity of 11 when adding then rejects it`() {
        classUnderTest.addIntensityPoint("a", NOW, 11)
    }

    @Test
    fun `Given an intensity of 0 when replacing then rejects it before touching the database`() {
        val result = runCatching { classUnderTest.replaceLatestIntensityPoint("a", NOW, 0) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(attackDao, never()).replaceLatestIntensityPoint(any())
    }

    @Test(expected = StorageFullDataException::class)
    fun `Given the disk is full when starting then throws StorageFullDataException`() {
        given(attackDao.startIfNoneInProgress(any())).willThrow(SQLiteFullException())

        classUnderTest.startIfNoneInProgress(NOW, "UTC")
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given another SQLite failure when ending then throws DatabaseDataException`() {
        doThrow(SQLiteException()).whenever(attackDao).end(any(), any(), any())

        classUnderTest.end("a", NOW)
    }

    @Test
    fun `When ending then writes the end and the update time`() {
        classUnderTest.end("a", NOW)

        verify(attackDao).end("a", NOW.toEpochMilli(), NOW.toEpochMilli())
    }

    @Test
    fun `Given the DAO refuses to reopen when reopening then AnotherInProgress`() {
        given(attackDao.reopenIfNoneInProgress("a", NOW.toEpochMilli())).willReturn(false)

        assertEquals(ReopenDataModel.AnotherInProgress, classUnderTest.reopen("a"))
    }

    @Test
    fun `Given the DAO reopens when reopening then Reopened`() {
        given(attackDao.reopenIfNoneInProgress("a", NOW.toEpochMilli())).willReturn(true)

        assertEquals(ReopenDataModel.Reopened, classUnderTest.reopen("a"))
    }

    @Test
    fun `Given a previous end when looking it up then returns it as an instant`() {
        given(attackDao.previousEndBefore("a", NOW.toEpochMilli())).willReturn(5_000L)

        assertEquals(Instant.ofEpochMilli(5_000L), classUnderTest.previousEndBefore("a", NOW))
    }

    @Test
    fun `Given no previous end when looking it up then null`() {
        given(attackDao.previousEndBefore("a", NOW.toEpochMilli())).willReturn(null)

        assertNull(classUnderTest.previousEndBefore("a", NOW))
    }

    @Test
    fun `When updating the long attack check then writes both fields`() {
        classUnderTest.updateLongAttackCheck("a", confirmedAt = NOW, disabled = false)

        verify(attackDao).updateLongCheck("a", NOW.toEpochMilli(), false, NOW.toEpochMilli())
    }

    @Test
    fun `When asking for ended attacks touching a range then queries in epoch millis`() {
        val from = Instant.parse("2026-10-22T19:00:00Z")
        val to = Instant.parse("2026-10-23T19:00:00Z")
        given(
            attackDao.endedTouching(from.toEpochMilli(), to.toEpochMilli())
        ).willReturn(emptyList())

        assertEquals(emptyList<Any>(), classUnderTest.endedTouching(from, to))
    }

    @Test
    fun `When counting then asks the DAO for attacks that are not deleted`() {
        given(attackDao.count()).willReturn(5)

        assertEquals(5, classUnderTest.count())
    }
}
