package com.migrely.app.datasource.attack.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** DAO queries added for past attacks and merging (change add-past-attacks). */
@RunWith(AndroidJUnit4::class)
class AttackDaoPastAttacksTest {
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
    fun givenSeveralOverlaps_whenListing_thenAllComeOldestFirst() {
        classUnderTest.insertAttack(attack(id = "later", start = 5_000L, end = null))
        classUnderTest.insertAttack(attack(id = "earlier", start = 1_000L, end = 2_000L))
        classUnderTest.insertAttack(attack(id = "touching", start = 500L, end = 1_000L))

        val actual = classUnderTest.overlappingAll(null, 1_000L, 6_000L)

        assertEquals(listOf("earlier", "later"), actual.map { it.attack.id })
    }

    @Test
    fun givenAnExcludedAttack_whenListing_thenItIsLeftOut() {
        classUnderTest.insertAttack(attack(id = "this", start = 1_000L, end = 3_000L))
        classUnderTest.insertAttack(attack(id = "other", start = 3_500L, end = 4_000L))

        val actual = classUnderTest.overlappingAll("this", 1_000L, 5_000L)

        assertEquals(listOf("other"), actual.map { it.attack.id })
    }

    @Test
    fun givenADeletedAttack_whenListing_thenItIsHidden() {
        classUnderTest.insertAttack(attack(id = "gone", start = 1_000L, end = 2_000L))
        classUnderTest.softDelete("gone", 9_000L)

        assertTrue(classUnderTest.overlappingAll(null, 1_500L, 3_000L).isEmpty())
    }

    @Test
    fun givenNoOverlap_whenInsertingAnEndedAttack_thenStored() {
        classUnderTest.insertAttack(attack(id = "earlier", start = 1_000L, end = 2_000L))

        assertTrue(classUnderTest.insertEndedIfNoOverlap(attack("new", 2_000L, 3_000L)))
        assertEquals(3_000L, classUnderTest.byId("new")?.attack?.endEpochMillis)
    }

    @Test
    fun givenAnOverlap_whenInsertingAnEndedAttack_thenNothingIsStored() {
        classUnderTest.insertAttack(attack(id = "open", start = 2_500L, end = null))

        assertFalse(classUnderTest.insertEndedIfNoOverlap(attack("new", 2_000L, 3_000L)))
        assertNull(classUnderTest.byId("new"))
    }

    @Test
    fun givenTwoAttacks_whenMerging_thenOneHoldsEverything() {
        classUnderTest.insertAttack(attack(id = "survivor", start = 1_000L, end = 2_000L))
        classUnderTest.insertAttack(
            attack(id = "absorbed", start = 3_000L, end = null).copy(
                longCheckConfirmedEpochMillis = 7_000L,
                longCheckDisabled = true
            )
        )
        classUnderTest.insertIntensityPoint(point("survivor", 1_500L, 4))
        classUnderTest.insertIntensityPoint(point("absorbed", 3_500L, 8))
        classUnderTest.insertTag(AttackTagLocalModel("survivor", "SYMPTOM", "aura"))
        classUnderTest.insertTag(AttackTagLocalModel("absorbed", "SYMPTOM", "aura"))
        classUnderTest.insertTag(AttackTagLocalModel("absorbed", "TRIGGER", "custom:t"))

        classUnderTest.mergeInto(
            survivorId = "survivor",
            absorbedIds = listOf("absorbed"),
            startEpochMillis = 900L,
            startZoneId = "Asia/Tokyo",
            endEpochMillis = null,
            impact = 3,
            note = "Joined",
            nowEpochMillis = 9_000L
        )

        val merged = checkNotNull(classUnderTest.byId("survivor"))
        assertEquals(900L, merged.attack.startEpochMillis)
        assertEquals("Asia/Tokyo", merged.attack.startZoneId)
        assertNull(merged.attack.endEpochMillis)
        assertEquals(3, merged.attack.functionalImpact)
        assertEquals("Joined", merged.attack.note)
        assertEquals(7_000L, merged.attack.longCheckConfirmedEpochMillis)
        assertTrue(merged.attack.longCheckDisabled)
        assertEquals(
            listOf(1_500L to 4, 3_500L to 8),
            merged.intensityPoints.map { it.atEpochMillis to it.value }.sortedBy { it.first }
        )
        assertEquals(
            setOf("SYMPTOM" to "aura", "TRIGGER" to "custom:t"),
            merged.tags.map { it.kind to it.tagKey }.toSet()
        )
        assertNull(classUnderTest.byId("absorbed"))
        assertNull(classUnderTest.deletedById("absorbed"))
        assertEquals("survivor", classUnderTest.inProgress()?.attack?.id)
    }

    @Test
    fun givenNothingToAbsorb_whenMerging_thenOnlyTheFieldsChange() {
        classUnderTest.insertAttack(attack(id = "survivor", start = 1_000L, end = 2_000L))
        classUnderTest.insertIntensityPoint(point("survivor", 1_500L, 4))

        classUnderTest.mergeInto("survivor", emptyList(), 800L, "UTC", 2_500L, null, "", 9_000L)

        val merged = checkNotNull(classUnderTest.byId("survivor"))
        assertEquals(800L to 2_500L, merged.attack.startEpochMillis to merged.attack.endEpochMillis)
        assertEquals(1, merged.intensityPoints.size)
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
