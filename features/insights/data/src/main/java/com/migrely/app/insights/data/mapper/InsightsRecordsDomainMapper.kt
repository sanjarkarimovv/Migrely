package com.migrely.app.insights.data.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.insights.domain.model.InsightsAttackDomainModel
import com.migrely.app.insights.domain.model.InsightsEffectDomainModel
import com.migrely.app.insights.domain.model.InsightsIntakeDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationKindDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsDomainModel
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The records Insights uses. Triggers become the English words the app shows, as on the doctor report; the user's own
 * names stay as typed, and a custom trigger that cannot be found is left out.
 */
class InsightsRecordsDomainMapper {
    fun toDomain(records: BackupRecordsDataModel): InsightsRecordsDomainModel {
        val tagNames = records.customTags.associate { it.id to it.name }
        val medications = records.medications.associateBy { it.id }
        return InsightsRecordsDomainModel(
            attacks = records.attacks.map { attack ->
                InsightsAttackDomainModel(
                    start = attack.start,
                    zone = zoneOf(attack.startZoneId),
                    end = attack.end,
                    functionalImpact = attack.functionalImpact,
                    triggers = attack.tags.mapNotNull { triggerOf(it, tagNames) }
                )
            },
            intakes = records.intakes.mapNotNull { intake ->
                medications[intake.medicationId]?.let { medication ->
                    InsightsIntakeDomainModel(
                        medicationName = medication.name,
                        kind = when (medication.kind) {
                            MedicationKindDataModel.Acute -> InsightsMedicationKindDomainModel.Acute
                            MedicationKindDataModel.Preventive ->
                                InsightsMedicationKindDomainModel.Preventive
                        },
                        takenAt = intake.takenAt,
                        zone = zoneOf(intake.takenZoneId),
                        effect = when (intake.effect) {
                            IntakeEffectDataModel.NotAtAll -> InsightsEffectDomainModel.NotAtAll
                            IntakeEffectDataModel.ALittle -> InsightsEffectDomainModel.ALittle
                            IntakeEffectDataModel.ALot -> InsightsEffectDomainModel.ALot
                            IntakeEffectDataModel.Completely -> InsightsEffectDomainModel.Completely
                            null -> null
                        }
                    )
                }
            },
            periodDates = records.dayMarks.filter { it.period }.map { it.date }.toSet()
        )
    }

    /**
     * A zone id saved on another device may be unknown to this device's time-zone data, for example in a database
     * restored from a phone with newer tzdata, or not a zone id at all; UTC keeps the instant correct.
     */
    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
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
}
