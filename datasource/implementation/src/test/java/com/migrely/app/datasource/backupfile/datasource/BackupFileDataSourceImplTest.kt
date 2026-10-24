package com.migrely.app.datasource.backupfile.datasource

import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.RECORDS
import com.migrely.app.datasource.backupfile.mapper.BackupFileMapper
import com.migrely.app.datasource.backupfile.model.BackupDecodedDataModel
import com.migrely.app.datasource.backupfile.validator.BackupRecordsValidator
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import java.io.File
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private val EXPORTED_AT = Instant.parse("2026-10-25T08:00:00Z")

class BackupFileDataSourceImplTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val classUnderTest by lazy {
        BackupFileDataSourceImpl(
            folder.root,
            BackupFileMapper(
                AttackTagLocalMapper(),
                CustomTagLocalMapper(),
                MedicationLocalMapper()
            ),
            BackupRecordsValidator()
        )
    }

    @Test
    fun `When a backup is written and read back then every record is as it was`() {
        val text = classUnderTest.encode(RECORDS, EXPORTED_AT)

        assertTrue(text.contains("\"format\":\"migrely-backup\""))
        assertTrue(text.contains("\"schemaVersion\":6"))
        assertTrue(text.contains("\"exportedAt\":\"2026-10-25T08:00:00Z\""))
        assertEquals(BackupDecodedDataModel.Records(RECORDS, 6), classUnderTest.decode(text))
    }

    @Test
    fun `Given a byte-order mark when reading then it is ignored`() {
        val text = Char(0xFEFF) + classUnderTest.encode(RECORDS, EXPORTED_AT)

        assertEquals(BackupDecodedDataModel.Records(RECORDS, 6), classUnderTest.decode(text))
    }

    @Test
    fun `Given text that is not a Migrely backup when reading then not a backup`() {
        listOf(
            "",
            "hello",
            "[1, 2]",
            "{\"format\":\"other\",\"schemaVersion\":1}",
            "{\"schemaVersion\":6}",
            "{\"format\":\"migrely-backup\"}",
            "{\"format\":\"migrely-backup\",\"schemaVersion\":0,\"exportedAt\":\"x\"}",
            "{\"format\":\"migrely-backup\",\"schemaVersion\":6}",
            "{\"format\":\"migrely-backup\",\"schemaVersion\":\"six\",\"exportedAt\":\"x\"}"
        ).forEach {
            assertEquals(it, BackupDecodedDataModel.NotABackup, classUnderTest.decode(it))
        }
    }

    @Test
    fun `Given a newer version wrote it when reading then newer, whatever it holds`() {
        assertEquals(
            BackupDecodedDataModel.Newer,
            classUnderTest.decode(
                "{\"format\":\"migrely-backup\",\"schemaVersion\":7,\"attacks\":{\"new\":true}}"
            )
        )
    }

    @Test
    fun `Given an older backup without newer fields when reading then they take defaults`() {
        val text = """
            {"format":"migrely-backup","schemaVersion":4,"exportedAt":"2026-01-01T00:00:00Z",
             "attacks":[{"id":"a","startEpochMillis":1000,"startZoneId":"Asia/Tashkent",
               "createdEpochMillis":1000,"updatedEpochMillis":1000}],
             "medications":[{"id":"m","name":" Ibuprofen ","kind":"ACUTE",
               "createdEpochMillis":1,"updatedEpochMillis":1}],
             "intakes":[{"id":"i","medicationId":"m","takenEpochMillis":5,"takenZoneId":"UTC",
               "createdEpochMillis":5}],
             "unknownField":1}
        """.trimIndent()

        val decoded = classUnderTest.decode(text) as BackupDecodedDataModel.Records

        assertEquals(4, decoded.schemaVersion)
        val attack = decoded.records.attacks.single()
        assertEquals(null, attack.end)
        assertEquals(emptyList<Any>(), attack.tags)
        assertEquals(null, attack.functionalImpact)
        assertEquals("Ibuprofen", decoded.records.medications.single().name)
        assertEquals(null, decoded.records.intakes.single().effect)
        assertEquals(emptyList<Any>(), decoded.records.dayMarks)
    }

    @Test
    fun `Given a value this version cannot store when reading then not a backup`() {
        val text = classUnderTest.encode(RECORDS, EXPORTED_AT)
        listOf(
            "\"key\":\"nausea\"" to "\"key\":\"sneezing\"",
            "\"startZoneId\":\"Asia/Tashkent\"" to "\"startZoneId\":\"Mars/Base\"",
            "\"date\":\"2026-10-18\"" to "\"date\":\"18.10.2026\"",
            "\"kind\":\"ACUTE\"" to "\"kind\":\"RESCUE\"",
            "\"effect\":\"A_LOT\"" to "\"effect\":\"SOMEWHAT\"",
            "\"medicationId\":\"m1\"" to "\"medicationId\":\"missing\""
        ).forEach { (from, to) ->
            assertTrue(from, text.contains(from))
            assertEquals(
                to,
                BackupDecodedDataModel.NotABackup,
                classUnderTest.decode(text.replaceFirst(from, to))
            )
        }
    }

    @Test
    fun `When automatic backups are saved then the newest three stay and none is partial`() {
        (1..5).forEach {
            classUnderTest.saveAutomatic("backup $it", Instant.ofEpochMilli(it * 1_000L))
        }

        val names = File(folder.root, "backups").list().orEmpty().sorted()
        assertEquals(
            listOf("migrely-auto-3000.json", "migrely-auto-4000.json", "migrely-auto-5000.json"),
            names
        )
        assertEquals("backup 5", File(folder.root, "backups/migrely-auto-5000.json").readText())
    }

    @Test
    fun `The schema version is the database's latest`() {
        val latest = File(
            "schemas/com.migrely.app.datasource.database.MigrelyDatabase"
        ).list().orEmpty()
            .mapNotNull { it.removeSuffix(".json").toIntOrNull() }
            .max()

        assertEquals(latest, BACKUP_SCHEMA_VERSION)
    }

    @Test
    fun `Given nothing on the phone when written and read then an empty backup`() {
        val empty =
            BackupRecordsDataModel(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

        assertEquals(
            BackupDecodedDataModel.Records(empty, 6),
            classUnderTest.decode(classUnderTest.encode(empty, EXPORTED_AT))
        )
    }
}
