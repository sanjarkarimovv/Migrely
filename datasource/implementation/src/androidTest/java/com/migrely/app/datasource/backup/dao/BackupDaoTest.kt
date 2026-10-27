package com.migrely.app.datasource.backup.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.backup.model.AddedLocalModel
import com.migrely.app.datasource.backup.model.BackupLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val HOUR = 3_600_000L

private fun attack(id: String, start: Long, end: Long?, deleted: Long? = null) = AttackLocalModel(
    id = id,
    startEpochMillis = start,
    startZoneId = "Asia/Tashkent",
    endEpochMillis = end,
    longCheckConfirmedEpochMillis = null,
    longCheckDisabled = false,
    createdEpochMillis = start,
    updatedEpochMillis = start,
    deletedEpochMillis = deleted
)

private fun medication(id: String, name: String) =
    MedicationLocalModel(id, name, name.lowercase(), "ACUTE", "SIMPLE", null, false, 1L, 1L)

private fun intake(id: String, medicationId: String, attackId: String?, taken: Long) =
    MedicationIntakeLocalModel(id, medicationId, attackId, taken, "Asia/Tashkent", null, taken)

private fun customTag(id: String, name: String) =
    CustomTagLocalModel(id, "TRIGGER", name, name.lowercase(), false, 1L)

private fun records(
    attacks: List<AttackLocalModel> = emptyList(),
    points: List<IntensityPointLocalModel> = emptyList(),
    tags: List<AttackTagLocalModel> = emptyList(),
    customTags: List<CustomTagLocalModel> = emptyList(),
    medications: List<MedicationLocalModel> = emptyList(),
    intakes: List<MedicationIntakeLocalModel> = emptyList(),
    dayMarks: List<DayMarkLocalModel> = emptyList()
) = BackupLocalModel(attacks, points, tags, customTags, medications, intakes, dayMarks)

/** Backups read and write every health table at once (change add-data-export-import). */
@RunWith(AndroidJUnit4::class)
class BackupDaoTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: BackupDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.backupDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    /** Every row of [table], hidden ones included. */
    private fun rows(table: String): Int = database.query("SELECT COUNT(*) FROM $table", null).use {
        it.moveToFirst()
        it.getInt(0)
    }

    private val phone = records(
        attacks = listOf(
            attack("p1", 10 * HOUR, 12 * HOUR),
            attack(
                "gone",
                20 * HOUR,
                21 * HOUR,
                deleted =
                22 * HOUR
            )
        ),
        points = listOf(
            IntensityPointLocalModel(0, "p1", 10 * HOUR, 5),
            IntensityPointLocalModel(
                0,
                "gone",
                20 * HOUR,
                3
            )
        ),
        tags = listOf(
            AttackTagLocalModel("p1", "TRIGGER", "custom:pt"),
            AttackTagLocalModel("gone", "SYMPTOM", "aura")
        ),
        customTags = listOf(customTag("pt", "Exams")),
        medications = listOf(medication("pm", "Ibuprofen")),
        intakes = listOf(
            intake("pi", "pm", "p1", 11 * HOUR),
            intake("gi", "pm", "gone", 20 * HOUR)
        ),
        dayMarks = listOf(DayMarkLocalModel("2026-10-18", period = true, painFree = false, 1L))
    )

    @Test
    fun givenAnAttackBeingDeleted_whenReading_thenItAndWhatHangsOnItAreLeftOut() {
        classUnderTest.replaceAll(phone)

        val read = classUnderTest.records()

        assertEquals(listOf("p1"), read.attacks.map { it.id })
        assertEquals(listOf("p1"), read.intensityPoints.map { it.attackId })
        assertEquals(listOf("p1"), read.attackTags.map { it.attackId })
        assertEquals(listOf("pi"), read.intakes.map { it.id })
        assertEquals(phone.customTags, read.customTags)
        assertEquals(phone.medications, read.medications)
        assertEquals(phone.dayMarks, read.dayMarks)
    }

    @Test
    fun whenAskingForRecords_thenAnyHealthRowCounts() {
        assertFalse(classUnderTest.hasRecords())

        classUnderTest.replaceAll(records(dayMarks = phone.dayMarks))
        assertTrue(classUnderTest.hasRecords())

        classUnderTest.replaceAll(records(attacks = listOf(attack("x", 1L, null, deleted = 2L))))
        assertFalse(classUnderTest.hasRecords())
    }

    @Test
    fun whenReplacing_thenOnlyTheBackupIsLeftAndDeviceStateStays() {
        classUnderTest.replaceAll(phone)
        database.medicationDao().upsertOveruseNotice(
            OveruseNoticeLocalModel("SIMPLE", "REACHED", 5L)
        )
        val backup = records(
            attacks = listOf(attack("b1", 30 * HOUR, 31 * HOUR)),
            medications = listOf(medication("bm", "Sumatriptan")),
            intakes = listOf(intake("bi", "bm", "b1", 30 * HOUR))
        )

        classUnderTest.replaceAll(backup)

        val read = classUnderTest.records()
        assertEquals(listOf("b1"), read.attacks.map { it.id })
        assertEquals(listOf("bm"), read.medications.map { it.id })
        assertEquals(listOf("bi"), read.intakes.map { it.id })
        assertEquals(
            emptyList<Any>(),
            read.customTags + read.dayMarks + read.attackTags + read.intensityPoints
        )
        assertEquals(1, database.medicationDao().overuseNotices().size)
    }

    @Test
    fun givenABackupTheDatabaseRefuses_whenReplacing_thenNothingChanges() {
        classUnderTest.replaceAll(phone)
        val before = classUnderTest.records()

        try {
            classUnderTest.replaceAll(
                records(medications = listOf(medication("x1", "Same"), medication("x2", "Same")))
            )
            fail("A repeated name must be refused")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // The whole transaction is rolled back.
        }

        assertEquals(before, classUnderTest.records())
    }

    @Test
    fun whenAdding_thenRecordsOnThePhoneAreSkippedAndNamesStandForThePhonesOnes() {
        classUnderTest.replaceAll(phone)
        val backup = records(
            attacks = listOf(
                attack("p1", 10 * HOUR, 12 * HOUR), // the same attack
                attack("other", 11 * HOUR, 13 * HOUR), // logged on another phone, overlapping p1
                attack("new", 40 * HOUR, 41 * HOUR)
            ),
            points = listOf(
                IntensityPointLocalModel(0, "new", 40 * HOUR, 6),
                IntensityPointLocalModel(
                    0,
                    "other",
                    11 * HOUR,
                    9
                )
            ),
            tags = listOf(
                AttackTagLocalModel("new", "TRIGGER", "custom:bt"),
                AttackTagLocalModel("new", "SYMPTOM", "aura")
            ),
            customTags = listOf(customTag("bt", "EXAMS")),
            medications = listOf(medication("bm", "ibuprofen"), medication("bn", "Naproxen")),
            intakes = listOf(
                intake("pi", "bm", "p1", 11 * HOUR), // the same intake
                intake("dup", "bm", null, 11 * HOUR), // another ID, same medication and time
                intake("oi", "bm", "other", 12 * HOUR), // of the skipped attack
                intake("ni", "bn", "new", 40 * HOUR)
            ),
            dayMarks = listOf(
                DayMarkLocalModel("2026-10-18", period = false, painFree = true, 2L),
                DayMarkLocalModel("2026-10-19", period = true, painFree = false, 2L)
            )
        )

        // Added besides the attack: Naproxen, the intakes "oi" and "ni", and 19 October's mark.
        assertEquals(
            AddedLocalModel(addedAttacks = 1, skippedAttacks = 2, addedOthers = 4),
            classUnderTest.add(backup)
        )

        val read = classUnderTest.records()
        assertEquals(listOf("p1", "new"), read.attacks.map { it.id })
        assertEquals(listOf("pt"), read.customTags.map { it.id })
        assertEquals(setOf("pm", "bn"), read.medications.map { it.id }.toSet())
        assertEquals(
            setOf(
                AttackTagLocalModel("new", "TRIGGER", "custom:pt"),
                AttackTagLocalModel("new", "SYMPTOM", "aura")
            ),
            read.attackTags.filter { it.attackId == "new" }.toSet()
        )
        assertEquals(listOf(5, 6), read.intensityPoints.map { it.value })
        assertEquals(
            listOf(
                intake("pi", "pm", "p1", 11 * HOUR),
                intake("oi", "pm", null, 12 * HOUR),
                intake("ni", "bn", "new", 40 * HOUR)
            ),
            read.intakes
        )
        assertEquals(
            listOf(
                DayMarkLocalModel("2026-10-18", period = true, painFree = false, 1L),
                DayMarkLocalModel("2026-10-19", period = true, painFree = false, 2L)
            ),
            read.dayMarks
        )
    }

    @Test
    fun givenAnAttackInProgressOnThePhone_whenAddingALaterOne_thenItIsSkipped() {
        classUnderTest.replaceAll(records(attacks = listOf(attack("now", 10 * HOUR, null))))

        val added = classUnderTest.add(
            records(
                attacks = listOf(
                    attack("before", 5 * HOUR, 6 * HOUR),
                    attack(
                        "after",
                        50 * HOUR,
                        51 * HOUR
                    )
                )
            )
        )

        assertEquals(AddedLocalModel(addedAttacks = 1, skippedAttacks = 1, addedOthers = 0), added)
        assertEquals(listOf("before", "now"), classUnderTest.records().attacks.map { it.id })
    }

    @Test
    fun givenAnAttackInProgressInTheBackup_whenAdding_thenItReachesIntoTheFuture() {
        classUnderTest.replaceAll(records(attacks = listOf(attack("later", 50 * HOUR, 51 * HOUR))))

        val added = classUnderTest.add(records(attacks = listOf(attack("now", 10 * HOUR, null))))

        assertEquals(AddedLocalModel(addedAttacks = 0, skippedAttacks = 1, addedOthers = 0), added)
    }

    @Test
    fun givenOverlapsInsideTheBackup_whenAdding_thenOnlyTheFirstIsAdded() {
        val added = classUnderTest.add(
            records(
                attacks = listOf(
                    attack("a", 10 * HOUR, 12 * HOUR),
                    attack(
                        "b",
                        11 * HOUR,
                        13 * HOUR
                    )
                )
            )
        )

        assertEquals(AddedLocalModel(addedAttacks = 1, skippedAttacks = 1, addedOthers = 0), added)
        assertEquals(listOf("a"), classUnderTest.records().attacks.map { it.id })
    }

    /** QA #58: a deleted attack, however long ago, lingers hidden until purged; the backup's copy takes its place. */
    @Test
    fun givenAnAttackBeingDeletedWithTheSameId_whenAdding_thenTheBackupsCopyTakesItsPlace() {
        classUnderTest.replaceAll(
            records(
                attacks = listOf(attack("gone", 10 * HOUR, 12 * HOUR, deleted = 13 * HOUR)),
                points = listOf(IntensityPointLocalModel(0, "gone", 10 * HOUR, 3)),
                tags = listOf(AttackTagLocalModel("gone", "SYMPTOM", "aura")),
                medications = listOf(medication("pm", "Ibuprofen")),
                intakes = listOf(intake("i", "pm", "gone", 11 * HOUR))
            )
        )
        val backup = records(
            attacks = listOf(attack("gone", 10 * HOUR, 12 * HOUR)),
            points = listOf(IntensityPointLocalModel(0, "gone", 10 * HOUR, 7)),
            tags = listOf(AttackTagLocalModel("gone", "TRIGGER", "stress")),
            medications = listOf(medication("pm", "Ibuprofen")),
            intakes = listOf(intake("i", "pm", "gone", 11 * HOUR))
        )

        val added = classUnderTest.add(backup)

        // The intake was hidden with the attack, so it comes back too, linked to it.
        assertEquals(AddedLocalModel(addedAttacks = 1, skippedAttacks = 0, addedOthers = 1), added)
        val read = classUnderTest.records()
        assertEquals(backup.attacks, read.attacks)
        assertEquals(listOf(7), read.intensityPoints.map { it.value })
        assertEquals(backup.attackTags, read.attackTags)
        assertEquals(backup.intakes, read.intakes)
        assertEquals(1, rows("attack"))
    }

    @Test
    fun givenAnIntakeHiddenWithAnotherAttackBeingDeleted_whenAdding_thenTheBackupsCopyIsAdded() {
        // On the phone, the backup's "a" was merged into "b", which took its intake, and "b" was then deleted.
        classUnderTest.replaceAll(
            records(
                attacks = listOf(attack("b", 9 * HOUR, 12 * HOUR, deleted = 13 * HOUR)),
                medications = listOf(medication("pm", "Ibuprofen")),
                intakes = listOf(
                    intake("i", "pm", "b", 11 * HOUR),
                    intake("j", "pm", "b", 9 * HOUR)
                )
            )
        )

        val added = classUnderTest.add(
            records(
                attacks = listOf(attack("a", 10 * HOUR, 12 * HOUR)),
                medications = listOf(medication("pm", "Ibuprofen")),
                intakes = listOf(
                    intake("i", "pm", "a", 11 * HOUR), // the same ID as one hidden
                    intake("k", "pm", null, 9 * HOUR) // the same medication and time as one hidden
                )
            )
        )

        assertEquals(AddedLocalModel(addedAttacks = 1, skippedAttacks = 0, addedOthers = 2), added)
        assertEquals(
            listOf(intake("k", "pm", null, 9 * HOUR), intake("i", "pm", "a", 11 * HOUR)),
            classUnderTest.records().intakes
        )
        // "b" stays hidden, for its undo or its purge, without the intakes the backup brought back.
        assertEquals(2, rows("attack"))
        assertEquals(2, rows("medication_intake"))
    }

    @Test
    fun givenAnAttackBeingDeleted_whenReplacing_thenItGoesToo() {
        // The phone's "gone" is being deleted, with a point, a tag and the intake "gi".
        classUnderTest.replaceAll(phone)

        classUnderTest.replaceAll(records(attacks = listOf(attack("gone", 20 * HOUR, 21 * HOUR))))

        assertEquals(
            listOf(attack("gone", 20 * HOUR, 21 * HOUR)),
            classUnderTest.records().attacks
        )
        assertEquals(
            listOf(1, 0, 0, 0),
            listOf("attack", "intensity_point", "attack_tag", "medication_intake").map { rows(it) }
        )
    }

    @Test
    fun givenTwoBackupTagsForOnePhoneTag_whenAdding_thenTheAttackGetsItOnce() {
        classUnderTest.replaceAll(records(customTags = listOf(customTag("pt", "Exams"))))

        classUnderTest.add(
            records(
                attacks = listOf(attack("new", 10 * HOUR, 11 * HOUR)),
                tags = listOf(
                    AttackTagLocalModel("new", "TRIGGER", "custom:pt"),
                    AttackTagLocalModel("new", "TRIGGER", "custom:bt")
                ),
                // One stands for the phone's tag by its ID, the other by its name.
                customTags = listOf(customTag("pt", "Other"), customTag("bt", "Exams"))
            )
        )

        assertEquals(
            listOf(AttackTagLocalModel("new", "TRIGGER", "custom:pt")),
            classUnderTest.records().attackTags
        )
    }

    @Test
    fun givenABackupTheDatabaseRefuses_whenAdding_thenNothingChanges() {
        classUnderTest.replaceAll(phone)
        val before = classUnderTest.records()

        try {
            classUnderTest.add(
                records(
                    attacks = listOf(attack("new", 40 * HOUR, 41 * HOUR)),
                    intakes = listOf(intake("x", "no-such-medication", null, 40 * HOUR))
                )
            )
            fail("An intake of no medication must be refused")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // The whole transaction is rolled back, the new attack with it.
        }

        assertEquals(before, classUnderTest.records())
    }
}
