package com.migrely.app.datasource.backup.roundtrip

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.backup.datasource.BackupDataSourceImpl
import com.migrely.app.datasource.backup.mapper.BackupLocalMapper
import com.migrely.app.datasource.backup.model.AddedRecordsDataModel
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSourceImpl
import com.migrely.app.datasource.backupfile.mapper.BackupFileMapper
import com.migrely.app.datasource.backupfile.model.BackupDecodedDataModel
import com.migrely.app.datasource.backupfile.validator.BackupRecordsValidator
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import java.io.File
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val HOUR = 3_600_000L

private fun attack(id: String, start: Long, end: Long?) = AttackLocalModel(
    id = id,
    startEpochMillis = start,
    startZoneId = "Asia/Tashkent",
    endEpochMillis = end,
    longCheckConfirmedEpochMillis = null,
    longCheckDisabled = false,
    createdEpochMillis = start,
    updatedEpochMillis = start + 1,
    functionalImpact = 2,
    note = "Ko‘p uxlamadim"
)

private fun medication(
    id: String,
    name: String,
    kind: String,
    category: String?,
    archived: Boolean
) = MedicationLocalModel(id, name, name.lowercase(), kind, category, null, archived, 1L, 2L)

/**
 * A backup the app writes is read back the same, restores the same records on another phone, and adds nothing to the
 * phone it came from (change add-data-export-import, review). The rows are written through the app's own DAOs.
 */
@RunWith(AndroidJUnit4::class)
class BackupRoundTripTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var phone: MigrelyDatabase
    private lateinit var newPhone: MigrelyDatabase
    private val localMapper = BackupLocalMapper(
        AttackTagLocalMapper(),
        CustomTagLocalMapper(),
        MedicationLocalMapper(),
        DayMarkLocalMapper()
    )
    private val files = BackupFileDataSourceImpl(
        File(context.cacheDir, "round-trip"),
        BackupFileMapper(AttackTagLocalMapper(), CustomTagLocalMapper(), MedicationLocalMapper()),
        BackupRecordsValidator()
    )

    @Before
    fun setUp() {
        phone = Room.inMemoryDatabaseBuilder(context, MigrelyDatabase::class.java).build()
        newPhone = Room.inMemoryDatabaseBuilder(context, MigrelyDatabase::class.java).build()
        with(phone.tagDao()) {
            insert(CustomTagLocalModel("t1", "SYMPTOM", "Yawning", "yawning", false, 1L))
            insert(CustomTagLocalModel("t2", "TRIGGER", "Yawning", "yawning", true, 2L))
        }
        with(phone.medicationDao()) {
            insert(medication("m1", "Ibuprofen", "ACUTE", "SIMPLE", archived = false))
            insert(medication("m2", "Topiramate", "PREVENTIVE", null, archived = true))
        }
        with(phone.attackDao()) {
            insertAttack(attack("ended", 10 * HOUR, 12 * HOUR))
            insertIntensityPoint(IntensityPointLocalModel(0, "ended", 10 * HOUR, 4))
            insertIntensityPoint(IntensityPointLocalModel(0, "ended", 11 * HOUR, 8))
            insertTag(AttackTagLocalModel("ended", "LOCATION", "left"))
            insertTag(AttackTagLocalModel("ended", "SYMPTOM", "custom:t1"))
            insertTag(AttackTagLocalModel("ended", "TRIGGER", "stress"))
            insertAttack(attack("deleting", 20 * HOUR, 21 * HOUR))
            softDelete("deleting", 22 * HOUR)
            insertAttack(attack("now", 30 * HOUR, null))
        }
        with(phone.medicationDao()) {
            insertIntake(
                MedicationIntakeLocalModel(
                    "i1", "m1", "ended", 11 * HOUR, "Asia/Tashkent", "400 mg",
                    11 * HOUR, "A_LOT", true
                )
            )
            insertIntake(
                MedicationIntakeLocalModel(
                    "i2",
                    "m2",
                    null,
                    5 * HOUR,
                    "Europe/Berlin",
                    null,
                    5 * HOUR
                )
            )
            insertIntake(
                MedicationIntakeLocalModel(
                    "i3",
                    "m1",
                    "deleting",
                    20 * HOUR,
                    "Asia/Tashkent",
                    null,
                    20 * HOUR
                )
            )
        }
        phone.dayMarkDao().upsert(
            DayMarkLocalModel("2026-10-18", period = true, painFree = true, 1L)
        )
    }

    @After
    fun tearDown() {
        phone.close()
        newPhone.close()
    }

    @Test
    fun whenExportedReadAndRestored_thenTheSameRecords_andAddingThemBackChangesNothing() {
        val source = BackupDataSourceImpl(phone.backupDao(), localMapper, phone::deleteEverything)
        val exported = source.records()
        assertEquals(listOf("ended", "now"), exported.attacks.map { it.id })
        assertEquals(listOf("i2", "i1"), exported.intakes.map { it.id })

        val decoded = files.decode(files.encode(exported, Instant.EPOCH))
        assertEquals(BackupDecodedDataModel.Records(exported, 6), decoded)

        val restored =
            BackupDataSourceImpl(newPhone.backupDao(), localMapper, newPhone::deleteEverything)
        restored.replaceAll((decoded as BackupDecodedDataModel.Records).records)
        assertEquals(exported, restored.records())

        assertEquals(
            AddedRecordsDataModel(addedAttacks = 0, skippedAttacks = 2, addedOthers = 0),
            source.add(exported)
        )
        assertEquals(exported, source.records())
    }

    /**
     * QA #58, BRD D118: a migraine deleted after the export, and not undone, lingers hidden until it is purged. Adding
     * the backup brings it back as it was, with its points, tags and intake.
     */
    @Test
    fun givenAMigraineDeletedAfterTheExport_whenAddingTheBackup_thenItIsBack() {
        val source = BackupDataSourceImpl(phone.backupDao(), localMapper, phone::deleteEverything)
        val backup = files.decode(files.encode(source.records(), Instant.EPOCH))
            as BackupDecodedDataModel.Records
        phone.attackDao().softDelete("ended", 40 * HOUR)
        assertEquals(listOf("now"), source.records().attacks.map { it.id })

        val added = source.add(backup.records)

        assertEquals(
            AddedRecordsDataModel(addedAttacks = 1, skippedAttacks = 1, addedOthers = 1),
            added
        )
        assertEquals(backup.records, source.records())
    }

    /** Delete all data (change add-settings-and-safety, BRD D96): the device's own state goes with the records. */
    @Test
    fun whenEverythingIsDeleted_thenNoTableKeepsARow() {
        phone.reminderSettingsDao().update { it.copy(dailyEnabled = true) }
        phone.medicationDao().upsertOveruseNotice(OveruseNoticeLocalModel("SIMPLE", "SOFT", 1L))
        val source = BackupDataSourceImpl(phone.backupDao(), localMapper, phone::deleteEverything)
        phone.query("SELECT COUNT(*) FROM sqlite_sequence", null).use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }

        source.deleteAll()

        assertEquals(false, source.hasRecords())
        listOf(
            "attack",
            "intensity_point",
            "attack_tag",
            "custom_tag",
            "medication",
            "medication_intake",
            "overuse_notice",
            "day_mark",
            "reminder_settings",
            // The intensity points' AUTOINCREMENT counter, which would tell how many were ever logged.
            "sqlite_sequence"
        ).forEach { table ->
            phone.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
                cursor.moveToFirst()
                assertEquals(table, 0, cursor.getInt(0))
            }
        }
    }
}
