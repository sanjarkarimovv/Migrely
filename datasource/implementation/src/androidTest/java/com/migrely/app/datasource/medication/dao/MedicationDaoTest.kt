package com.migrely.app.datasource.medication.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Medication and intake queries (change add-medication-tracking). */
@RunWith(AndroidJUnit4::class)
class MedicationDaoTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: MedicationDao
    private lateinit var attackDao: AttackDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.medicationDao()
        attackDao = database.attackDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun medication(
        id: String,
        name: String,
        archived: Boolean = false,
        kind: String = "ACUTE",
        category: String? = "SIMPLE"
    ) = MedicationLocalModel(
        id = id,
        name = name,
        nameKey = name.lowercase(),
        kind = kind,
        category = category,
        defaultDose = null,
        archived = archived,
        createdEpochMillis = 1L,
        updatedEpochMillis = 1L
    )

    private fun intake(id: String, medicationId: String, attackId: String?, at: Long) =
        MedicationIntakeLocalModel(id, medicationId, attackId, at, "Asia/Tashkent", "400 mg", at)

    private fun attack(id: String, start: Long) = AttackLocalModel(
        id = id,
        startEpochMillis = start,
        startZoneId = "Asia/Tashkent",
        endEpochMillis = start + 1_000L,
        longCheckConfirmedEpochMillis = null,
        longCheckDisabled = false,
        createdEpochMillis = start,
        updatedEpochMillis = start
    )

    @Test
    fun givenMedications_whenListingActive_thenRecentlyTakenFirstThenByNameWithoutArchived() {
        classUnderTest.insert(medication("z", "Zolmitriptan"))
        classUnderTest.insert(medication("a", "aspirin"))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insert(medication("p", "Paracetamol", archived = true))
        classUnderTest.insertIntake(intake("1", "z", null, 5_000L))
        classUnderTest.insertIntake(intake("2", "i", null, 9_000L))

        assertEquals(listOf("i", "z", "a"), classUnderTest.activeByLastUse().map { it.id })
    }

    @Test
    fun givenAnArchivedMedication_whenCountingAndLookingUp_thenCountSkipsItAndLookupFindsIt() {
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insert(medication("p", "Paracetamol", archived = true))

        assertEquals(1, classUnderTest.countActive())
        assertEquals("p", classUnderTest.byNameKey("paracetamol")?.id)
        assertNull(classUnderTest.byNameKey("aspirin"))
    }

    @Test
    fun givenAnArchivedMedication_whenUpdating_thenItStaysArchivedWithTheNewFields() {
        classUnderTest.insert(medication("p", "Paracetamol", archived = true))

        val changed = classUnderTest.update("p", "Panadol", "panadol", "ACUTE", null, "500 mg", 7L)

        assertEquals(1, changed)
        val stored = checkNotNull(classUnderTest.byId("p"))
        assertEquals(
            listOf("Panadol", "panadol", null, "500 mg"),
            listOf(stored.name, stored.nameKey, stored.category, stored.defaultDose)
        )
        // Only "Undo" or adding the name again brings it back, after the limit check (D35).
        assertEquals(true, stored.archived)
        assertEquals(0, classUnderTest.update("missing", "X", "x", "ACUTE", null, null, 7L))
    }

    @Test
    fun givenIntakes_whenReadingAnAttacksIntakes_thenOldestFirstWithTheCurrentName() {
        attackDao.insertAttack(attack("a", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("late", "i", "a", 8_000L))
        classUnderTest.insertIntake(intake("early", "i", "a", 2_000L))
        classUnderTest.insertIntake(intake("alone", "i", null, 3_000L))
        classUnderTest.update("i", "Ibuprofen 400", "ibuprofen 400", "ACUTE", "SIMPLE", null, 9L)

        val actual = classUnderTest.intakesOfAttack("a")

        assertEquals(listOf("early", "late"), actual.map { it.id })
        assertEquals("Ibuprofen 400", actual.first().medicationName)
    }

    @Test
    fun givenADeletedAttackWithIntakes_whenRestoredInTime_thenItsIntakesAreBack() {
        attackDao.insertAttack(attack("a", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", "a", 2_000L))

        attackDao.softDelete("a", 5_000L)
        attackDao.clearDeleted("a", 6_000L)

        assertEquals(listOf("1"), classUnderTest.intakesOfAttack("a").map { it.id })
    }

    @Test
    fun givenADeletedAttackWithIntakes_whenPurged_thenItsIntakesGoAndOthersStay() {
        attackDao.insertAttack(attack("a", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", "a", 2_000L))
        classUnderTest.insertIntake(intake("2", "i", null, 3_000L))

        attackDao.softDelete("a", 5_000L)
        attackDao.purgeDeletedBefore(6_000L)

        assertEquals(listOf("2"), intakeAttackIds().keys.toList())
        assertEquals(listOf("i"), classUnderTest.activeByLastUse().map { it.id })
    }

    @Test
    fun givenAnAttackStartedByMistakeWithAnIntake_whenDiscarded_thenTheIntakeStaysOnItsOwn() {
        attackDao.insertAttack(attack("a", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", "a", 2_000L))

        attackDao.discard("a")

        assertNull(attackDao.byId("a"))
        assertEquals(mapOf<String, String?>("1" to null), intakeAttackIds())
    }

    @Test
    fun givenIntakesInAndAroundTheWindow_whenReadingThePendingQuestion_thenTheOldestOpenAcuteOne() {
        attackDao.insertAttack(attack("gone", 1_000L).copy(deletedEpochMillis = 9_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insert(medication("t", "Topiramate", kind = "PREVENTIVE", category = null))
        classUnderTest.insertIntake(intake("before", "i", null, 999L))
        classUnderTest.insertIntake(intake("preventive", "t", null, 1_000L))
        classUnderTest.insertIntake(intake("deleted", "i", "gone", 1_100L))
        classUnderTest.insertIntake(intake("answered", "i", null, 1_200L).copy(effect = "A_LOT"))
        classUnderTest.insertIntake(intake("skipped", "i", null, 1_300L).copy(effectClosed = true))
        classUnderTest.insertIntake(intake("second", "i", null, 1_500L))
        classUnderTest.insertIntake(intake("first", "i", null, 1_400L))
        classUnderTest.insertIntake(intake("at end", "i", null, 2_000L))

        val actual = classUnderTest.pendingEffectQuestion(1_000L, 2_000L)

        assertEquals("first", actual?.id)
        assertEquals("Ibuprofen", actual?.medicationName)
        assertEquals("400 mg", actual?.dose)
    }

    @Test
    fun givenIntakes_whenReadingOneIntakesQuestion_thenOnlyThatOneWhileOpenAcuteAndInTheWindow() {
        attackDao.insertAttack(attack("gone", 1_000L).copy(deletedEpochMillis = 9_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insert(medication("t", "Topiramate", kind = "PREVENTIVE", category = null))
        classUnderTest.insertIntake(intake("older", "i", null, 1_100L))
        classUnderTest.insertIntake(intake("asked", "i", null, 1_500L))
        classUnderTest.insertIntake(intake("preventive", "t", null, 1_500L))
        classUnderTest.insertIntake(intake("deleted", "i", "gone", 1_500L))
        classUnderTest.insertIntake(intake("answered", "i", null, 1_500L).copy(effect = "A_LOT"))

        // An older open question does not stand in for the one asked about (review of add-reminders).
        assertEquals("asked", classUnderTest.pendingEffectQuestionOf("asked", 1_000L, 2_000L)?.id)
        listOf("preventive", "deleted", "answered").forEach {
            assertNull(classUnderTest.pendingEffectQuestionOf(it, 1_000L, 2_000L))
        }
        assertNull(classUnderTest.pendingEffectQuestionOf("asked", 1_600L, 2_000L))
    }

    @Test
    fun givenAnIntake_whenAnsweredThenCleared_thenTheQuestionStaysClosed() {
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", null, 1_000L))
        classUnderTest.insertIntake(intake("2", "i", null, 1_100L))

        classUnderTest.setEffect("1", "COMPLETELY")
        classUnderTest.closeEffectQuestion("2")

        assertNull(classUnderTest.pendingEffectQuestion(0L, 5_000L))
        assertEquals(listOf("COMPLETELY" to true, null to true), effects())
        classUnderTest.setEffect("1", null)
        assertEquals(listOf(null to true, null to true), effects())
    }

    @Test
    fun givenAnAttacksIntake_whenAnswered_thenTheAttacksIntakesCarryTheAnswer() {
        attackDao.insertAttack(attack("a", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", "a", 2_000L))

        classUnderTest.setEffect("1", "A_LITTLE")

        val actual = classUnderTest.intakesOfAttack("a").single()
        assertEquals("A_LITTLE" to true, actual.effect to actual.effectClosed)
    }

    @Test
    fun givenIntakes_whenReadingAcuteOnesInAWindow_thenWithoutPreventiveDeletedOrOutside() {
        attackDao.insertAttack(attack("gone", 1_000L).copy(deletedEpochMillis = 9_000L))
        attackDao.insertAttack(attack("kept", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insert(medication("s", "Sumatriptan", category = "TRIPTAN"))
        classUnderTest.insert(medication("n", "Mystery", category = null))
        classUnderTest.insert(medication("t", "Topiramate", kind = "PREVENTIVE", category = null))
        classUnderTest.insertIntake(intake("1", "i", null, 999L))
        classUnderTest.insertIntake(intake("2", "i", "kept", 1_000L))
        classUnderTest.insertIntake(intake("3", "s", null, 1_500L))
        classUnderTest.insertIntake(intake("4", "n", null, 1_600L))
        classUnderTest.insertIntake(intake("5", "t", null, 1_700L))
        classUnderTest.insertIntake(intake("6", "i", "gone", 1_800L))
        classUnderTest.insertIntake(intake("7", "i", null, 2_000L))

        val actual = classUnderTest.acuteIntakesBetween(1_000L, 2_000L)

        assertEquals(
            listOf(1_000L to "SIMPLE", 1_500L to "TRIPTAN", 1_600L to null),
            actual.map { it.takenEpochMillis to it.category }.sortedBy { it.first }
        )
    }

    @Test
    fun givenANotice_whenAcknowledgedAgain_thenTheLatestTimeReplacesIt() {
        classUnderTest.upsertOveruseNotice(OveruseNoticeLocalModel("TRIPTAN", "SOFT", 1_000L))
        classUnderTest.upsertOveruseNotice(OveruseNoticeLocalModel("TRIPTAN", "DOCTOR", 2_000L))
        classUnderTest.upsertOveruseNotice(OveruseNoticeLocalModel("TRIPTAN", "SOFT", 3_000L))

        assertEquals(
            setOf(
                OveruseNoticeLocalModel("TRIPTAN", "SOFT", 3_000L),
                OveruseNoticeLocalModel("TRIPTAN", "DOCTOR", 2_000L)
            ),
            classUnderTest.overuseNotices().toSet()
        )
    }

    @Test(expected = SQLiteConstraintException::class)
    fun givenAMedication_whenInsertingTheSameNameKey_thenRefused() {
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insert(medication("j", "ibuprofen"))
    }

    private fun effects(): List<Pair<String?, Boolean>> = database.query(
        "SELECT effect, effect_closed FROM medication_intake ORDER BY id",
        null
    ).use {
        buildList {
            while (it.moveToNext()) {
                add((if (it.isNull(0)) null else it.getString(0)) to (it.getInt(1) == 1))
            }
        }
    }

    private fun intakeAttackIds(): Map<String, String?> =
        database.query("SELECT id, attack_id FROM medication_intake ORDER BY id", null).use {
            buildMap {
                while (it.moveToNext()) {
                    put(
                        it.getString(0),
                        if (it.isNull(1)) null else it.getString(1)
                    )
                }
            }
        }

    @Test
    fun givenTwoAttacksWithIntakes_whenMerging_thenTheIntakesMoveToTheSurvivor() {
        attackDao.insertAttack(attack("survivor", 1_000L))
        attackDao.insertAttack(attack("absorbed", 5_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", "survivor", 1_500L))
        classUnderTest.insertIntake(intake("2", "i", "absorbed", 5_500L))

        attackDao.mergeInto(
            "survivor",
            listOf("absorbed"),
            1_000L,
            "Asia/Tashkent",
            6_000L,
            null,
            "",
            9_000L
        )

        assertEquals(listOf("1", "2"), classUnderTest.intakesOfAttack("survivor").map { it.id })
    }

    @Test
    fun givenAnIntake_whenDeletingIt_thenItIsGone() {
        attackDao.insertAttack(attack("a", 1_000L))
        classUnderTest.insert(medication("i", "Ibuprofen"))
        classUnderTest.insertIntake(intake("1", "i", "a", 2_000L))

        classUnderTest.deleteIntake("1")

        assertEquals(emptyList<String>(), classUnderTest.intakesOfAttack("a").map { it.id })
    }
}
