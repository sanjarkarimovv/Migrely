package com.migrely.app.datasource.attack.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.EarliestStartLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** DAO queries added for History (change add-attack-history). */
@RunWith(AndroidJUnit4::class)
class AttackDaoHistoryTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: AttackDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.attackDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenNoAttacks_whenReadingTheEarliestStart_thenNull() {
        assertNull(classUnderTest.earliestStart())
    }

    @Test
    fun givenSeveralAttacks_whenReadingTheEarliestStart_thenTheFirstWithItsZone() {
        classUnderTest.insertAttack(attack("later", 5_000L, "Asia/Tashkent"))
        classUnderTest.insertAttack(attack("first", 1_000L, "Asia/Tokyo"))
        classUnderTest.insertAttack(attack("middle", 3_000L, "Asia/Tashkent"))

        assertEquals(EarliestStartLocalModel(1_000L, "Asia/Tokyo"), classUnderTest.earliestStart())
    }

    @Test
    fun givenTheFirstAttackIsDeleted_whenReadingTheEarliestStart_thenItIsSkipped() {
        classUnderTest.insertAttack(attack("first", 1_000L, "Asia/Tokyo"))
        classUnderTest.insertAttack(attack("second", 3_000L, "Asia/Tashkent"))
        classUnderTest.softDelete("first", 9_000L)

        assertEquals(
            EarliestStartLocalModel(3_000L, "Asia/Tashkent"),
            classUnderTest.earliestStart()
        )
    }

    private fun attack(id: String, start: Long, zone: String) = AttackLocalModel(
        id = id,
        startEpochMillis = start,
        startZoneId = zone,
        endEpochMillis = start + 500L,
        longCheckConfirmedEpochMillis = null,
        longCheckDisabled = false,
        createdEpochMillis = start,
        updatedEpochMillis = start
    )
}
