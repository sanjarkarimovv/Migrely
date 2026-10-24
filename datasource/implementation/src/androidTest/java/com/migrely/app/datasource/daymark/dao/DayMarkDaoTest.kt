package com.migrely.app.datasource.daymark.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Day mark queries (change add-day-markers). */
@RunWith(AndroidJUnit4::class)
class DayMarkDaoTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: DayMarkDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.dayMarkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenNoRow_whenAMarkIsSet_thenARowWithThatFlagAndTime() {
        classUnderTest.update("2026-10-14", 7L) { it.copy(period = true) }

        assertEquals(
            DayMarkLocalModel("2026-10-14", period = true, painFree = false, 7L),
            classUnderTest.byDate("2026-10-14")
        )
    }

    @Test
    fun givenBothMarks_whenOneIsRemoved_thenTheOtherStays() {
        classUnderTest.update("2026-10-14", 7L) { it.copy(period = true) }
        classUnderTest.update("2026-10-14", 8L) { it.copy(painFree = true) }

        classUnderTest.update("2026-10-14", 9L) { it.copy(period = false) }

        assertEquals(
            DayMarkLocalModel("2026-10-14", period = false, painFree = true, 9L),
            classUnderTest.byDate("2026-10-14")
        )
    }

    @Test
    fun givenOneMark_whenItIsRemoved_thenTheRowGoes() {
        classUnderTest.update("2026-10-14", 7L) { it.copy(painFree = true) }

        classUnderTest.update("2026-10-14", 8L) { it.copy(painFree = false) }

        assertNull(classUnderTest.byDate("2026-10-14"))
    }

    @Test
    fun givenMarks_whenReadingARange_thenBothEndsIncludedInDateOrder() {
        listOf(
            "2026-10-31",
            "2026-09-30",
            "2026-10-01",
            "2026-11-01",
            "2026-10-15"
        ).forEach { date ->
            classUnderTest.update(date, 1L) { it.copy(period = true) }
        }

        assertEquals(
            listOf("2026-10-01", "2026-10-15", "2026-10-31"),
            classUnderTest.between("2026-10-01", "2026-10-31").map { it.date }
        )
    }
}
