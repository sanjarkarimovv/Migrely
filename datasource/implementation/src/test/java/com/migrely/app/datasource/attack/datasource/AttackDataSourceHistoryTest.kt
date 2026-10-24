package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.attack.model.EarliestAttackDataModel
import com.migrely.app.datasource.attack.model.EarliestStartLocalModel
import com.migrely.app.datasource.exception.DatabaseDataException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AttackDataSourceHistoryTest {
    private val attackDao: AttackDao = mock()
    private val classUnderTest = AttackDataSourceImpl(
        attackDao = attackDao,
        attackLocalMapper = AttackLocalMapper(),
        clock = Clock.fixed(Instant.parse("2026-10-23T09:00:00Z"), ZoneOffset.UTC),
        generateId = { "new" }
    )

    @Test
    fun `Given an attack when reading the earliest start then its instant and zone`() {
        given(attackDao.earliestStart()).willReturn(EarliestStartLocalModel(1_000L, "Asia/Tokyo"))

        assertEquals(
            EarliestAttackDataModel(Instant.ofEpochMilli(1_000L), "Asia/Tokyo"),
            classUnderTest.earliestStart()
        )
    }

    @Test
    fun `Given no attack when reading the earliest start then null`() {
        assertNull(classUnderTest.earliestStart())
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given the database fails when reading the earliest start then a database failure`() {
        whenever(attackDao.earliestStart()).thenThrow(SQLiteException())

        classUnderTest.earliestStart()
    }
}
