package com.migrely.app.insights.data.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.backup.model.BackupAttackDataModel
import com.migrely.app.datasource.backup.model.BackupCustomTagDataModel
import com.migrely.app.datasource.backup.model.BackupDayMarkDataModel
import com.migrely.app.datasource.backup.model.BackupIntakeDataModel
import com.migrely.app.datasource.backup.model.BackupMedicationDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import com.migrely.app.insights.domain.model.InsightsAttackDomainModel
import com.migrely.app.insights.domain.model.InsightsEffectDomainModel
import com.migrely.app.insights.domain.model.InsightsIntakeDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationKindDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-20T04:00:00Z")
private val END: Instant = Instant.parse("2026-10-20T07:00:00Z")
private val CREATED: Instant = Instant.parse("2026-10-20T08:00:00Z")

private fun attack(
    tags: List<AttackTagDataModel>,
    end: Instant? = END,
    impact: Int? = 3,
    zone: String = "Asia/Tashkent"
) = BackupAttackDataModel(
    id = "a",
    start = START,
    startZoneId = zone,
    end = end,
    intensityPoints = listOf(IntensityPointDataModel(START, 7)),
    longCheckConfirmedAt = null,
    longCheckDisabled = false,
    tags = tags,
    functionalImpact = impact,
    note = "A note",
    createdAt = CREATED,
    updatedAt = CREATED
)

private fun medication(id: String, name: String, kind: MedicationKindDataModel) =
    BackupMedicationDataModel(
        id = id,
        name = name,
        kind = kind,
        category = MedicationCategoryDataModel.SimplePainkiller,
        defaultDose = null,
        archived = false,
        createdAt = CREATED,
        updatedAt = CREATED
    )

private fun intake(
    medicationId: String,
    effect: IntakeEffectDataModel?,
    zone: String = "Asia/Tokyo"
) = BackupIntakeDataModel(
    id = "i-$medicationId-$effect",
    medicationId = medicationId,
    attackId = null,
    takenAt = START,
    takenZoneId = zone,
    dose = "400 mg",
    effect = effect,
    effectClosed = effect != null,
    createdAt = CREATED
)

private fun records(
    attacks: List<BackupAttackDataModel> = emptyList(),
    customTags: List<BackupCustomTagDataModel> = emptyList(),
    medications: List<BackupMedicationDataModel> = emptyList(),
    intakes: List<BackupIntakeDataModel> = emptyList(),
    dayMarks: List<BackupDayMarkDataModel> = emptyList()
) = BackupRecordsDataModel(attacks, customTags, medications, intakes, dayMarks)

class InsightsRecordsDomainMapperTest {
    private val classUnderTest = InsightsRecordsDomainMapper()

    @Test
    fun `Built-in triggers become the words the app shows`() {
        val records = records(
            attacks = listOf(
                attack(BuiltInTriggerDataModel.entries.map(AttackTagDataModel::BuiltInTrigger))
            )
        )

        assertEquals(
            listOf(
                "Stress",
                "Too little sleep",
                "Too much sleep",
                "Skipped meal",
                "Dehydration",
                "Alcohol",
                "Caffeine",
                "Period",
                "Weather",
                "Bright light or screens",
                "Smell",
                "Physical effort",
                "Travel"
            ),
            classUnderTest.toDomain(records).attacks.single().triggers
        )
    }

    @Test
    fun `An attack keeps its times, zone, impact and triggers, the user's own by name`() {
        val records = records(
            attacks = listOf(
                attack(
                    listOf(
                        AttackTagDataModel.Location(HeadLocationDataModel.Left),
                        AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.Nausea),
                        AttackTagDataModel.CustomSymptom("s1"),
                        AttackTagDataModel.CustomTrigger("t1"),
                        AttackTagDataModel.CustomTrigger("missing"),
                        AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.Stress)
                    ),
                    end = null,
                    zone = "Asia/Tokyo"
                )
            ),
            customTags = listOf(
                BackupCustomTagDataModel(
                    "t1",
                    CustomTagKindDataModel.Trigger,
                    "Long drive",
                    false,
                    CREATED
                ),
                BackupCustomTagDataModel(
                    "s1",
                    CustomTagKindDataModel.Symptom,
                    "Jaw pain",
                    false,
                    CREATED
                )
            )
        )

        assertEquals(
            listOf(
                InsightsAttackDomainModel(
                    start = START,
                    zone = ZoneId.of("Asia/Tokyo"),
                    end = null,
                    functionalImpact = 3,
                    triggers = listOf("Long drive", "Stress")
                )
            ),
            classUnderTest.toDomain(records).attacks
        )
    }

    @Test
    fun `Intakes carry their medication's name and kind, the zone and the answer`() {
        val records = records(
            medications = listOf(
                medication("m1", "Ibuprofen", MedicationKindDataModel.Acute),
                medication("m2", "Propranolol", MedicationKindDataModel.Preventive)
            ),
            intakes = listOf(
                intake("m1", IntakeEffectDataModel.NotAtAll),
                intake("m1", IntakeEffectDataModel.ALittle),
                intake("m1", IntakeEffectDataModel.ALot),
                intake("m1", IntakeEffectDataModel.Completely),
                intake("m2", null, zone = "Asia/Tashkent"),
                intake("unknown", IntakeEffectDataModel.ALot)
            )
        )
        val tokyo = ZoneId.of("Asia/Tokyo")
        val acute = InsightsMedicationKindDomainModel.Acute

        assertEquals(
            listOf(
                InsightsIntakeDomainModel(
                    "Ibuprofen",
                    acute,
                    START,
                    tokyo,
                    InsightsEffectDomainModel.NotAtAll
                ),
                InsightsIntakeDomainModel(
                    "Ibuprofen",
                    acute,
                    START,
                    tokyo,
                    InsightsEffectDomainModel.ALittle
                ),
                InsightsIntakeDomainModel(
                    "Ibuprofen",
                    acute,
                    START,
                    tokyo,
                    InsightsEffectDomainModel.ALot
                ),
                InsightsIntakeDomainModel(
                    "Ibuprofen",
                    acute,
                    START,
                    tokyo,
                    InsightsEffectDomainModel.Completely
                ),
                InsightsIntakeDomainModel(
                    "Propranolol",
                    InsightsMedicationKindDomainModel.Preventive,
                    START,
                    ZoneId.of("Asia/Tashkent"),
                    null
                )
            ),
            classUnderTest.toDomain(records).intakes
        )
    }

    @Test
    fun `Only dates marked as a period are period dates`() {
        val records = records(
            dayMarks = listOf(
                BackupDayMarkDataModel(
                    LocalDate.of(2026, 10, 1),
                    period = true,
                    painFree = false,
                    CREATED
                ),
                BackupDayMarkDataModel(
                    LocalDate.of(2026, 10, 2),
                    period = true,
                    painFree = true,
                    CREATED
                ),
                BackupDayMarkDataModel(
                    LocalDate.of(2026, 10, 3),
                    period = false,
                    painFree = true,
                    CREATED
                )
            )
        )

        assertEquals(
            setOf(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2)),
            classUnderTest.toDomain(records).periodDates
        )
    }
}
