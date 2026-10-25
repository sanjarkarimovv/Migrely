package com.migrely.app.datasource.attack.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttackDaoTest {
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
    fun givenEightConcurrentStarts_thenExactlyOneAttackIsInProgress() {
        // Given
        val threads = 8
        val ready = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threads)
        val outcomes = (1..threads).map { index ->
            executor.submit<AttackDao.StartOutcome> {
                ready.await()
                classUnderTest.startIfNoneInProgress(
                    attack(id = "attack-$index", start = 1_000L, end = null)
                )
            }
        }

        // When
        ready.countDown()
        val created = outcomes.map { it.get(10, TimeUnit.SECONDS) }.count { it.created }
        executor.shutdown()

        // Then
        assertEquals(1, created)
        assertEquals(1, count("SELECT COUNT(*) FROM attack WHERE end_epoch_millis IS NULL"))
    }

    @Test
    fun givenAnAttackWithPoints_whenDeleted_thenItsPointsAreDeletedToo() {
        // Given
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = null))
        classUnderTest.insertIntensityPoint(
            IntensityPointLocalModel(attackId = "a", atEpochMillis = 2_000L, value = 6)
        )
        classUnderTest.insertIntensityPoint(
            IntensityPointLocalModel(attackId = "a", atEpochMillis = 3_000L, value = 9)
        )

        // When
        classUnderTest.delete("a")

        // Then
        assertEquals(0, count("SELECT COUNT(*) FROM intensity_point"))
    }

    @Test
    fun givenAnOvernightAttack_whenAskingForTheSecondDay_thenItIsReturned() {
        // Given: 23:30 to 02:00 in UTC+5, day boundaries of the second day in UTC
        val secondDayStart = Instant.parse("2026-10-22T19:00:00Z").toEpochMilli()
        val secondDayEnd = Instant.parse("2026-10-23T19:00:00Z").toEpochMilli()
        classUnderTest.insertAttack(
            attack(
                id = "overnight",
                start = Instant.parse("2026-10-22T18:30:00Z").toEpochMilli(),
                end = Instant.parse("2026-10-22T21:00:00Z").toEpochMilli()
            )
        )
        classUnderTest.insertAttack(
            attack(id = "in-progress", start = secondDayStart + 1_000L, end = null)
        )
        classUnderTest.insertAttack(
            attack(id = "ended-at-midnight", start = secondDayStart - 5_000L, end = secondDayStart)
        )

        // When
        val actual = classUnderTest.endedTouching(secondDayStart, secondDayEnd).map { it.attack.id }

        // Then
        assertEquals(listOf("overnight"), actual)
    }

    @Test
    fun givenEarlierAttacks_whenAskingForThePreviousEnd_thenReturnsTheLatestEndBeforeTheStart() {
        // Given
        classUnderTest.insertAttack(attack(id = "first", start = 1_000L, end = 2_000L))
        classUnderTest.insertAttack(attack(id = "second", start = 3_000L, end = 4_000L))
        classUnderTest.insertAttack(attack(id = "current", start = 5_000L, end = null))

        // When
        val actual = classUnderTest.previousEndBefore("current", 5_000L)

        // Then
        assertEquals(4_000L, actual)
    }

    @Test
    fun givenAPoint_whenReplacingTheLatest_thenUpdatesItInsteadOfAdding() {
        // Given
        classUnderTest.insertAttack(attack(id = "a", start = 1_000L, end = null))
        classUnderTest.insertIntensityPoint(
            IntensityPointLocalModel(attackId = "a", atEpochMillis = 2_000L, value = 9)
        )

        // When
        classUnderTest.replaceLatestIntensityPoint(
            IntensityPointLocalModel(attackId = "a", atEpochMillis = 2_060L, value = 6)
        )

        // Then
        val points = classUnderTest.byId("a")!!.intensityPoints
        assertEquals(1, points.size)
        assertEquals(6, points.single().value)
        assertEquals(2_060L, points.single().atEpochMillis)
    }

    @Test
    fun givenAnotherAttackInProgress_whenReopening_thenRefuses() {
        // Given
        classUnderTest.insertAttack(attack(id = "ended", start = 1_000L, end = 2_000L))
        classUnderTest.insertAttack(attack(id = "current", start = 3_000L, end = null))

        // When
        val reopened = classUnderTest.reopenIfNoneInProgress("ended", 4_000L)

        // Then
        assertFalse(reopened)
        assertEquals(2_000L, classUnderTest.byId("ended")!!.attack.endEpochMillis)
    }

    @Test
    fun givenNoneInProgress_whenReopening_thenTheAttackIsInProgressAgain() {
        // Given
        classUnderTest.insertAttack(attack(id = "ended", start = 1_000L, end = 2_000L))

        // When
        val reopened = classUnderTest.reopenIfNoneInProgress("ended", 4_000L)

        // Then
        assertTrue(reopened)
        assertNotNull(classUnderTest.inProgress())
    }

    private fun count(sql: String): Int =
        database.openHelper.readableDatabase.query(sql).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    /** The app's theme follows it (change add-settings-and-safety, BRD D94). */
    @Test
    fun whenAnAttackStartsEndsOrIsDeleted_thenWhetherOneIsInProgressFollows() = runBlocking {
        assertEquals(false, classUnderTest.inProgressChanges().first())

        classUnderTest.startIfNoneInProgress(attack(id = "now", start = 1_000L, end = null))
        assertEquals(true, classUnderTest.inProgressChanges().first())

        classUnderTest.end("now", endEpochMillis = 2_000L, nowEpochMillis = 2_000L)
        assertEquals(false, classUnderTest.inProgressChanges().first())

        classUnderTest.clearEnd("now", nowEpochMillis = 3_000L)
        classUnderTest.softDelete("now", nowEpochMillis = 4_000L)
        assertEquals(false, classUnderTest.inProgressChanges().first())
    }

    @Test
    fun givenEndedInProgressAndDeletedAttacks_whenCounting_thenDeletedOnesAreLeftOut() {
        // Given
        classUnderTest.insertAttack(attack(id = "ended", start = 1_000L, end = 2_000L))
        classUnderTest.insertAttack(attack(id = "deleted", start = 3_000L, end = 4_000L))
        classUnderTest.insertAttack(attack(id = "going", start = 5_000L, end = null))
        classUnderTest.softDelete("deleted", nowEpochMillis = 6_000L)

        // When
        val counted = classUnderTest.count()

        // Then
        assertEquals(2, counted)
    }

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
