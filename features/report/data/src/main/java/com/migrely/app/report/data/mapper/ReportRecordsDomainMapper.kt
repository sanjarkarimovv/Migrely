package com.migrely.app.report.data.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.report.domain.model.ReportAttackDomainModel
import com.migrely.app.report.domain.model.ReportEffectDomainModel
import com.migrely.app.report.domain.model.ReportIntakeDomainModel
import com.migrely.app.report.domain.model.ReportMedicationCategoryDomainModel
import com.migrely.app.report.domain.model.ReportMedicationKindDomainModel
import com.migrely.app.report.domain.model.ReportRecordsDomainModel
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The records a report uses. Tags and medications become the English words the app shows, since the report only lists
 * them; the user's own names stay as typed.
 */
class ReportRecordsDomainMapper {
    fun toDomain(records: BackupRecordsDataModel): ReportRecordsDomainModel {
        val tagNames = records.customTags.associate { it.id to it.name }
        val medications = records.medications.associateBy { it.id }
        val intakesByAttack = records.intakes.filter { it.attackId != null }.groupBy { it.attackId }
        return ReportRecordsDomainModel(
            attacks = records.attacks.map { attack ->
                ReportAttackDomainModel(
                    start = attack.start,
                    zone = zoneOf(attack.startZoneId),
                    end = attack.end,
                    peakIntensity = attack.intensityPoints.maxOfOrNull { it.value },
                    functionalImpact = attack.functionalImpact,
                    note = attack.note,
                    locations = attack.tags.filterIsInstance<AttackTagDataModel.Location>().map {
                        locationOf(it.location)
                    },
                    symptoms = attack.tags.mapNotNull { symptomOf(it, tagNames) },
                    triggers = attack.tags.mapNotNull { triggerOf(it, tagNames) },
                    medications = intakesByAttack[attack.id].orEmpty().sortedBy {
                        it.takenAt
                    }.mapNotNull { intake ->
                        medications[intake.medicationId]?.let {
                            listOfNotNull(it.name, intake.dose).joinToString(" ")
                        }
                    }
                )
            },
            intakes = records.intakes.mapNotNull { intake ->
                medications[intake.medicationId]?.let { medication ->
                    ReportIntakeDomainModel(
                        medicationName = medication.name,
                        kind = when (medication.kind) {
                            MedicationKindDataModel.Acute -> ReportMedicationKindDomainModel.Acute
                            MedicationKindDataModel.Preventive ->
                                ReportMedicationKindDomainModel.Preventive
                        },
                        category = when (medication.category) {
                            MedicationCategoryDataModel.SimplePainkiller ->
                                ReportMedicationCategoryDomainModel.SimplePainkiller
                            MedicationCategoryDataModel.Triptan ->
                                ReportMedicationCategoryDomainModel.Triptan
                            MedicationCategoryDataModel.CombinationOrOther ->
                                ReportMedicationCategoryDomainModel.CombinationOrOther
                            null -> null
                        },
                        takenAt = intake.takenAt,
                        zone = zoneOf(intake.takenZoneId),
                        effect = when (intake.effect) {
                            IntakeEffectDataModel.NotAtAll -> ReportEffectDomainModel.NotAtAll
                            IntakeEffectDataModel.ALittle -> ReportEffectDomainModel.ALittle
                            IntakeEffectDataModel.ALot -> ReportEffectDomainModel.ALot
                            IntakeEffectDataModel.Completely -> ReportEffectDomainModel.Completely
                            null -> null
                        }
                    )
                }
            },
            painFreeDates = records.dayMarks.filter { it.painFree }.map { it.date }.toSet()
        )
    }

    /**
     * A database restored from a phone with newer time-zone data can hold a zone this phone does not know; it is read
     * as UTC, as the rest of the app does, rather than stopping the report (review C2).
     */
    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }

    private fun symptomOf(tag: AttackTagDataModel, tagNames: Map<String, String>): String? =
        when (tag) {
            is AttackTagDataModel.BuiltInSymptom -> when (tag.symptom) {
                BuiltInSymptomDataModel.Aura -> "Aura"
                BuiltInSymptomDataModel.Nausea -> "Nausea"
                BuiltInSymptomDataModel.Vomiting -> "Vomiting"
                BuiltInSymptomDataModel.LightSensitivity -> "Sensitivity to light"
                BuiltInSymptomDataModel.SoundSensitivity -> "Sensitivity to sound"
                BuiltInSymptomDataModel.SmellSensitivity -> "Sensitivity to smell"
                BuiltInSymptomDataModel.Dizziness -> "Dizziness"
                BuiltInSymptomDataModel.NeckPain -> "Neck pain"
            }
            is AttackTagDataModel.CustomSymptom -> tagNames[tag.tagId]
            is AttackTagDataModel.Location,
            is AttackTagDataModel.BuiltInTrigger,
            is AttackTagDataModel.CustomTrigger -> null
        }

    private fun triggerOf(tag: AttackTagDataModel, tagNames: Map<String, String>): String? =
        when (tag) {
            is AttackTagDataModel.BuiltInTrigger -> when (tag.trigger) {
                BuiltInTriggerDataModel.Stress -> "Stress"
                BuiltInTriggerDataModel.TooLittleSleep -> "Too little sleep"
                BuiltInTriggerDataModel.TooMuchSleep -> "Too much sleep"
                BuiltInTriggerDataModel.SkippedMeal -> "Skipped meal"
                BuiltInTriggerDataModel.Dehydration -> "Dehydration"
                BuiltInTriggerDataModel.Alcohol -> "Alcohol"
                BuiltInTriggerDataModel.Caffeine -> "Caffeine"
                BuiltInTriggerDataModel.Period -> "Period"
                BuiltInTriggerDataModel.Weather -> "Weather"
                BuiltInTriggerDataModel.BrightLightOrScreens -> "Bright light or screens"
                BuiltInTriggerDataModel.Smell -> "Smell"
                BuiltInTriggerDataModel.PhysicalEffort -> "Physical effort"
                BuiltInTriggerDataModel.Travel -> "Travel"
            }
            is AttackTagDataModel.CustomTrigger -> tagNames[tag.tagId]
            is AttackTagDataModel.Location,
            is AttackTagDataModel.BuiltInSymptom,
            is AttackTagDataModel.CustomSymptom -> null
        }

    private fun locationOf(location: HeadLocationDataModel) = when (location) {
        HeadLocationDataModel.Left -> "Left side"
        HeadLocationDataModel.Right -> "Right side"
        HeadLocationDataModel.BothSides -> "Both sides"
        HeadLocationDataModel.Forehead -> "Forehead"
        HeadLocationDataModel.BackOfHead -> "Back of the head"
        HeadLocationDataModel.AroundEyes -> "Around the eyes"
        HeadLocationDataModel.WholeHead -> "Whole head"
    }
}
