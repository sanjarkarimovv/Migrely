package com.migrely.app.datasource.backupfile

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
import java.time.Instant
import java.time.LocalDate

private val START = Instant.parse("2026-10-20T04:00:00Z")

/** One of every kind of record, as a phone would hold them. */
val RECORDS = BackupRecordsDataModel(
    attacks = listOf(
        BackupAttackDataModel(
            id = "a1",
            start = START,
            startZoneId = "Asia/Tashkent",
            end = START.plusSeconds(3 * 3600),
            intensityPoints = listOf(
                IntensityPointDataModel(START, 4),
                IntensityPointDataModel(START.plusSeconds(3600), 7)
            ),
            longCheckConfirmedAt = null,
            longCheckDisabled = false,
            tags = listOf(
                AttackTagDataModel.Location(HeadLocationDataModel.Left),
                AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.Nausea),
                AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.Stress),
                AttackTagDataModel.CustomTrigger("t1")
            ),
            functionalImpact = 2,
            note = "Ko‘p uxlamadim, «shovqin»",
            createdAt = START,
            updatedAt = START.plusSeconds(60)
        ),
        BackupAttackDataModel(
            id = "a2",
            start = START.plusSeconds(86_400),
            startZoneId = "Europe/Berlin",
            end = null,
            intensityPoints = listOf(IntensityPointDataModel(START.plusSeconds(86_400), 10)),
            longCheckConfirmedAt = START.plusSeconds(90_000),
            longCheckDisabled = true,
            tags = emptyList(),
            functionalImpact = null,
            note = null,
            createdAt = START.plusSeconds(86_400),
            updatedAt = START.plusSeconds(86_400)
        )
    ),
    customTags = listOf(
        BackupCustomTagDataModel("t1", CustomTagKindDataModel.Trigger, "Exams", false, START),
        BackupCustomTagDataModel("t2", CustomTagKindDataModel.Symptom, "Yawning", true, START)
    ),
    medications = listOf(
        BackupMedicationDataModel(
            id = "m1",
            name = "Ibuprofen",
            kind = MedicationKindDataModel.Acute,
            category = MedicationCategoryDataModel.SimplePainkiller,
            defaultDose = "400 mg",
            archived = false,
            createdAt = START,
            updatedAt = START
        ),
        BackupMedicationDataModel(
            id = "m2",
            name = "Topiramate",
            kind = MedicationKindDataModel.Preventive,
            category = null,
            defaultDose = null,
            archived = true,
            createdAt = START,
            updatedAt = START
        )
    ),
    intakes = listOf(
        BackupIntakeDataModel(
            id = "i1",
            medicationId = "m1",
            attackId = "a1",
            takenAt = START.plusSeconds(600),
            takenZoneId = "Asia/Tashkent",
            dose = "400 mg",
            effect = IntakeEffectDataModel.ALot,
            effectClosed = true,
            createdAt = START.plusSeconds(600)
        ),
        BackupIntakeDataModel(
            id = "i2",
            medicationId = "m2",
            attackId = null,
            takenAt = START.minusSeconds(86_400),
            takenZoneId = "Asia/Tashkent",
            dose = null,
            effect = null,
            effectClosed = false,
            createdAt = START.minusSeconds(86_400)
        )
    ),
    dayMarks = listOf(
        BackupDayMarkDataModel(LocalDate.of(2026, 10, 18), period = true, painFree = true, START),
        BackupDayMarkDataModel(LocalDate.of(2026, 10, 19), period = false, painFree = true, START)
    )
)
