package com.migrely.app.datasource.attack.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** DAO queries added for the details screen (change add-attack-details). */
@RunWith(AndroidJUnit4::class)
class AttackDaoDetailsTest {
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
    fun givenAttacksThatOnlyTouch_whenCheckingOverlap_thenNoneOverlaps() {
        classUnderTest.insertAttack(attack(id = "earlier", start = 1_000L, end = 2_000L))

        assertNull(classUnderTest.overlapping("this", 2_000L, 3_000L))
        assertNull(classUnderTest.overlapping("this", 500L, 1_000L))
    }

    @Test
    fun givenAnOverlappingAttack_whenCheckingOverlap_thenReturnsIt() {
        classUnderTest.insertAttack(attack(id = "earlier", start = 1_000L, end = 2_000L))

        assertEquals("earlier", classUnderTest.overlapping("this", 1_999L, 3_000L)?.attack?.id)
    }

    @Test
    fun givenAnAttackInProgress_whenARangeStartsAfterIt_thenItOverlaps() {
        classUnderTest.insertAttack(attack(id = "open", start = 1_000L, end = null))

        assertEquals("open", classUnderTest.overlapping("this", 5_000L, 6_000L)?.attack?.id)
    }

    @Test
    fun givenAnAttack_whenListingOverlapsOfAMinute_thenItsStartIsInsideAndItsEndOutside() {
        // A medication's picked minute links to the attack it overlaps (EC-MED-09).
        classUnderTest.insertAttack(attack(id = "a", start = 61_000L, end = 180_000L))
        classUnderTest.insertAttack(attack(id = "open", start = 600_000L, end = null))

        assertEquals(listOf("a"), classUnderTest.overlappingAll(null, 60_000L, 120_000L).ids())
        assertEquals(
            emptyList<String>(),
            classUnderTest.overlappingAll(null, 180_000L, 240_000L).ids()
        )
        assertEquals(listOf("open"), classUnderTest.overlappingAll(null, 900_000L, 960_000L).ids())
    }

    private fun List<AttackWithPointsLocalModel>.ids() = map { it.attack.id }

    @Test
    fun givenADeletedAttack_whenCheckingOverlap_thenItIsIgnored() {
        classUnderTest.insertAttack(attack(id = "gone", start = 1_000L, end = 2_000L))
        classUnderTest.softDelete("gone", 9_000L)

        assertNull(classUnderTest.overlapping("this", 1_500L, 3_000L))
    }

    @Test
    fun givenPoints_whenShorteningAndRemovingOne_thenTheOtherStays() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = 10_000L))
        classUnderTest.insertIntensityPoint(point("a", 2_000L, 6))
        classUnderTest.insertIntensityPoint(point("a", 9_000L, 8))

        classUnderTest.updateTimesRemovingPoints(
            "a",
            1_000L,
            5_000L,
            listOf(point("a", 9_000L, 8)),
            11_000L
        )

        val stored = requireNotNull(classUnderTest.byId("a"))
        assertEquals(5_000L, stored.attack.endEpochMillis)
        assertEquals(listOf(2_000L), stored.intensityPoints.map { it.atEpochMillis })
    }

    @Test
    fun givenTwoEqualPoints_whenRemovingOne_thenOneRemains() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = 10_000L))
        classUnderTest.insertIntensityPoint(point("a", 2_000L, 6))
        classUnderTest.insertIntensityPoint(point("a", 2_000L, 6))

        classUnderTest.deleteIntensityPoint("a", 2_000L, 6)

        assertEquals(1, requireNotNull(classUnderTest.byId("a")).intensityPoints.size)
    }

    @Test
    fun givenADeletedAttack_whenReading_thenEveryQueryHidesIt() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = null))
        classUnderTest.softDelete("a", 2_000L)

        assertNull(classUnderTest.byId("a"))
        assertNull(classUnderTest.inProgress())
        assertNotNull(classUnderTest.recentlyDeleted(1_000L, 3_000L))
    }

    @Test
    fun givenADeletedTen_whenCheckingForAMaximum_thenItDoesNotCount() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = 2_000L))
        classUnderTest.insertIntensityPoint(point("a", 1_500L, 10))
        assertTrue(classUnderTest.anyIntensityPointOf(10))

        classUnderTest.softDelete("a", 3_000L)

        assertFalse(classUnderTest.anyIntensityPointOf(10))
    }

    @Test
    fun givenADeletionStampedAheadOfNow_whenReadingTheRecentOne_thenANewerRealOneIsFound() {
        classUnderTest.insertAttack(attack(id = "ahead", start = 1_000L, end = 2_000L))
        classUnderTest.insertAttack(attack(id = "real", start = 3_000L, end = 4_000L))
        // Deleted with the clock three days ahead, then the clock was set back (#112).
        classUnderTest.softDelete("ahead", 260_000_000L)
        classUnderTest.softDelete("real", 5_000L)

        assertEquals("real", classUnderTest.recentlyDeleted(1_000L, 7_000L)?.id)
    }

    @Test
    fun givenADeletionStampedAheadOfNow_whenPurging_thenItIsGoneForGood() {
        classUnderTest.insertAttack(attack(id = "ahead", start = 1_000L, end = 2_000L))
        classUnderTest.softDelete("ahead", 260_000_000L)

        classUnderTest.purgeDeletedOutside(1_000L, 7_000L)

        assertFalse(classUnderTest.restoreIfPossible("ahead", 5_000L))
        assertNull(classUnderTest.deletedById("ahead"))
    }

    @Test
    fun givenADeletion_whenPurgedAfterTheWindow_thenTheAttackAndItsRowsAreGone() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = 2_000L))
        classUnderTest.insertIntensityPoint(point("a", 1_500L, 5))
        classUnderTest.insertTag(AttackTagLocalModel("a", "SYMPTOM", "aura"))
        classUnderTest.softDelete("a", 3_000L)

        classUnderTest.purgeDeletedOutside(4_000L, 10_000L)

        assertFalse(classUnderTest.restoreIfPossible("a", 5_000L))
        assertEquals(0, count("SELECT COUNT(*) FROM intensity_point"))
        assertEquals(0, count("SELECT COUNT(*) FROM attack_tag"))
    }

    @Test
    fun givenADeletedAttackInProgress_whenAnotherStarted_thenRestoreIsRefused() {
        classUnderTest.insertAttack(attack(id = "old", start = 1_000L, end = null))
        classUnderTest.softDelete("old", 2_000L)
        classUnderTest.insertAttack(attack(id = "new", start = 3_000L, end = null))

        assertFalse(classUnderTest.restoreIfPossible("old", 4_000L))
    }

    @Test
    fun givenADeletedAttack_whenRestored_thenItIsVisibleWithItsDetails() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = 2_000L))
        classUnderTest.insertTag(AttackTagLocalModel("a", "LOCATION", "forehead"))
        classUnderTest.softDelete("a", 3_000L)

        assertTrue(classUnderTest.restoreIfPossible("a", 4_000L))

        assertEquals(
            listOf("forehead"),
            requireNotNull(classUnderTest.byId("a")).tags.map { it.tagKey }
        )
    }

    @Test
    fun givenASelectedTag_whenSelectedAgain_thenStoredOnce() {
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = 2_000L))

        classUnderTest.insertTag(AttackTagLocalModel("a", "SYMPTOM", "aura"))
        classUnderTest.insertTag(AttackTagLocalModel("a", "SYMPTOM", "aura"))

        assertEquals(1, count("SELECT COUNT(*) FROM attack_tag"))
    }

    private fun count(sql: String): Int =
        database.openHelper.readableDatabase.query(sql).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun point(attackId: String, at: Long, value: Int) =
        IntensityPointLocalModel(attackId = attackId, atEpochMillis = at, value = value)

    private fun attack(id: String, start: Long, end: Long?) = AttackLocalModel(
        id = id,
        startEpochMillis = start,
        startZoneId = "Asia/Tashkent",
        endEpochMillis = end,
        longCheckConfirmedEpochMillis = null,
        longCheckDisabled = false,
        createdEpochMillis = start,
        updatedEpochMillis = start
    )
}
