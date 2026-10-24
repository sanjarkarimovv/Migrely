package com.migrely.app.backup.data.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.backup.model.BackupAttackDataModel
import com.migrely.app.datasource.backup.model.BackupCustomTagDataModel
import com.migrely.app.datasource.backup.model.BackupIntakeDataModel
import com.migrely.app.datasource.backup.model.BackupMedicationDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val BOM = Char(0xFEFF).toString()
private val START = Instant.parse("2026-10-20T04:00:00Z")

private fun attack(
    id: String,
    start: Instant,
    end: Instant?,
    points: List<Int> = emptyList(),
    tags: List<AttackTagDataModel> = emptyList(),
    impact: Int? = null,
    note: String? = null
) = BackupAttackDataModel(
    id = id,
    start = start,
    startZoneId = "Asia/Tashkent",
    end = end,
    intensityPoints = points.map { IntensityPointDataModel(start, it) },
    longCheckConfirmedAt = null,
    longCheckDisabled = false,
    tags = tags,
    functionalImpact = impact,
    note = note,
    createdAt = start,
    updatedAt = start
)

private fun records(
    attacks: List<BackupAttackDataModel>,
    customTags: List<BackupCustomTagDataModel> = emptyList(),
    medications: List<BackupMedicationDataModel> = emptyList(),
    intakes: List<BackupIntakeDataModel> = emptyList()
) = BackupRecordsDataModel(attacks, customTags, medications, intakes, emptyList())

class AttacksCsvMapperTest {
    private val classUnderTest = AttacksCsvMapper()

    private fun lines(csv: String) = csv.removePrefix(BOM).removeSuffix("\r\n").split("\r\n")

    @Test
    fun `When writing then a byte-order mark, the header, and one row per attack, newest first`() {
        val csv = classUnderTest.toCsv(
            records(
                listOf(
                    attack("old", START, START.plusSeconds(3_600)),
                    attack("new", START.plusSeconds(86_400), null)
                )
            ),
            ','
        )

        assertTrue(csv.startsWith(BOM))
        assertTrue(csv.endsWith("\r\n"))
        assertEquals(
            listOf(
                "Start,End,Duration (minutes),Time zone,Peak pain,Pain level,Where it hurts," +
                    "Symptoms,Triggers,Medications,Impact,Note",
                "2026-10-21 09:00,,,Asia/Tashkent,,,,,,,,",
                "2026-10-20 09:00,2026-10-20 10:00,60,Asia/Tashkent,,,,,,,,"
            ),
            lines(csv)
        )
    }

    @Test
    fun `When writing an attack then its peak, level, tags, medications, impact and note`() {
        val csv = classUnderTest.toCsv(
            records(
                attacks = listOf(
                    attack(
                        id = "a",
                        start = START,
                        end = START.plusSeconds(5_400),
                        points = listOf(4, 7, 5),
                        tags = listOf(
                            AttackTagDataModel.Location(HeadLocationDataModel.Left),
                            AttackTagDataModel.Location(HeadLocationDataModel.AroundEyes),
                            AttackTagDataModel.BuiltInSymptom(
                                BuiltInSymptomDataModel.LightSensitivity
                            ),
                            AttackTagDataModel.CustomSymptom("s"),
                            AttackTagDataModel.BuiltInTrigger(
                                BuiltInTriggerDataModel.TooLittleSleep
                            ),
                            AttackTagDataModel.CustomTrigger("gone")
                        ),
                        impact = 3,
                        note = "Ko‘zim og‘ridi"
                    ),
                    attack(
                        "b",
                        START.minusSeconds(86_400),
                        START.minusSeconds(80_000),
                        points = listOf(2),
                        impact = 0
                    )
                ),
                customTags = listOf(
                    BackupCustomTagDataModel(
                        "s",
                        CustomTagKindDataModel.Symptom,
                        "Ko‘ngil aynishi",
                        false,
                        START
                    )
                ),
                medications = listOf(
                    BackupMedicationDataModel(
                        "m",
                        "Ibuprofen",
                        MedicationKindDataModel.Acute,
                        null,
                        null,
                        false,
                        START,
                        START
                    ),
                    BackupMedicationDataModel(
                        "n",
                        "Sumatriptan",
                        MedicationKindDataModel.Acute,
                        null,
                        null,
                        false,
                        START,
                        START
                    )
                ),
                intakes = listOf(
                    BackupIntakeDataModel(
                        "2", "m", "a",
                        START.plusSeconds(
                            600
                        ),
                        "UTC", "400 mg", null, false, START
                    ),
                    BackupIntakeDataModel(
                        "1", "n", "a",
                        START.plusSeconds(
                            60
                        ),
                        "UTC", null, null, false, START
                    ),
                    BackupIntakeDataModel(
                        "3", "m", null,
                        START.plusSeconds(
                            60
                        ),
                        "UTC", null, null, false, START
                    )
                )
            ),
            ','
        )

        assertEquals(
            listOf(
                "2026-10-20 09:00", "2026-10-20 10:30", "90", "Asia/Tashkent", "7", "Severe",
                "Left side; Around the eyes",
                "Sensitivity to light; Ko‘ngil aynishi",
                "Too little sleep",
                "Sumatriptan; Ibuprofen 400 mg", "Bed rest", "Ko‘zim og‘ridi"
            ).joinToString(","),
            lines(csv)[1]
        )
        assertTrue(lines(csv)[2].contains(",2,Mild,") && lines(csv)[2].contains(",No impact,"))
    }

    @Test
    fun `Given separators, quotes, lines or a formula start then quoted or made text`() {
        val csv = classUnderTest.toCsv(
            records(
                listOf(
                    attack("a", START, START.plusSeconds(60), note = "Bad, \"very\"\nbad"),
                    attack(
                        "b",
                        START.minusSeconds(600),
                        START.minusSeconds(60),
                        note = "=HYPERLINK(1)"
                    ),
                    attack(
                        "c",
                        START.minusSeconds(1_200),
                        START.minusSeconds(900),
                        note = "-2 hours of sleep"
                    )
                )
            ),
            ','
        )

        assertTrue(csv.contains(",\"Bad, \"\"very\"\"\nbad\"\r\n"))
        assertTrue(csv.contains(",'=HYPERLINK(1)\r\n"))
        assertTrue(csv.contains(",'-2 hours of sleep\r\n"))
    }

    @Test
    fun `Given a semicolon separator then lists joined by semicolons are quoted`() {
        val csv = classUnderTest.toCsv(
            records(
                listOf(
                    attack(
                        "a",
                        START,
                        START.plusSeconds(60),
                        tags = listOf(
                            AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.Stress),
                            AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.Weather)
                        )
                    )
                )
            ),
            ';'
        )

        assertEquals(
            "2026-10-20 09:00;2026-10-20 09:01;1;Asia/Tashkent;;;;;\"Stress; Weather\";;;",
            lines(csv)[1]
        )
        assertTrue(lines(csv)[0].startsWith("Start;End;"))
    }

    @Test
    fun `Given a zone this phone does not know then the times in UTC, and the zone named`() {
        val csv = classUnderTest.toCsv(
            records(
                listOf(attack("a", START, START.plusSeconds(60)).copy(startZoneId = "Mars/Olympus"))
            ),
            ','
        )

        assertTrue(lines(csv)[1].startsWith("2026-10-20 04:00,2026-10-20 04:01,1,Mars/Olympus,"))
    }

    @Test
    fun `Given no attacks then only the header`() {
        assertEquals(1, lines(classUnderTest.toCsv(records(emptyList()), ',')).size)
    }
}
