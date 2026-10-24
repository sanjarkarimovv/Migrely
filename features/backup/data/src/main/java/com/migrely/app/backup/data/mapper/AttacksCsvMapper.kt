package com.migrely.app.backup.data.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.backup.model.BackupAttackDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val LINE_END = "\r\n"
private const val LIST_SEPARATOR = "; "
private const val QUOTE = '"'
private const val MILD_UNTIL = 3
private const val MODERATE_UNTIL = 6
private const val IMPACT_NONE = 0
private const val IMPACT_LESS_ACTIVE = 1
private const val IMPACT_HAD_TO_STOP = 2
private const val IMPACT_BED_REST = 3

/** Lets spreadsheets read the file as UTF-8. An escape here would be stripped by the formatter. */
private val BYTE_ORDER_MARK = Char(0xFEFF).toString()

/** A cell starting with one of these would run as a formula in a spreadsheet, so it is written as text. */
private val FORMULA_STARTS = setOf('=', '+', '-', '@', '\t', '\r')
private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val HEADER = listOf(
    "Start",
    "End",
    "Duration (minutes)",
    "Time zone",
    "Peak pain",
    "Pain level",
    "Where it hurts",
    "Symptoms",
    "Triggers",
    "Medications",
    "Impact",
    "Note"
)

/**
 * One row per attack, newest first, for a spreadsheet (BRD BAK-F2, D72). It is a data file, so the column names and
 * the built-in labels are fixed English words, the same as the app shows; the user's own names are kept as typed.
 * Times are in the attack's own zone. Fields are quoted as RFC 4180 says, and the text starts with a byte-order mark so
 * spreadsheets read UTF-8.
 */
class AttacksCsvMapper {
    fun toCsv(records: BackupRecordsDataModel, separator: Char): String {
        val tagNames = records.customTags.associate { it.id to it.name }
        val medicationNames = records.medications.associate { it.id to it.name }
        val intakesByAttack = records.intakes.filter { it.attackId != null }.groupBy { it.attackId }
        val rows = records.attacks.sortedByDescending(BackupAttackDataModel::start).map { attack ->
            val zone = zoneOf(attack.startZoneId)
            val peak = attack.intensityPoints.maxOfOrNull { it.value }
            listOf(
                time(attack.start, zone),
                attack.end?.let { time(it, zone) }.orEmpty(),
                attack.end?.let {
                    Duration.between(attack.start, it).toMinutes().toString()
                }.orEmpty(),
                attack.startZoneId,
                peak?.toString().orEmpty(),
                peak?.let(::levelOf).orEmpty(),
                attack.tags.filterIsInstance<AttackTagDataModel.Location>()
                    .joinToString(LIST_SEPARATOR) { locationOf(it.location) },
                attack.tags.mapNotNull { symptomOf(it, tagNames) }.joinToString(LIST_SEPARATOR),
                attack.tags.mapNotNull { triggerOf(it, tagNames) }.joinToString(LIST_SEPARATOR),
                intakesByAttack[attack.id].orEmpty().sortedBy { it.takenAt }
                    .mapNotNull { intake ->
                        medicationNames[intake.medicationId]?.let { name ->
                            listOfNotNull(name, intake.dose).joinToString(" ")
                        }
                    }
                    .joinToString(LIST_SEPARATOR),
                attack.functionalImpact?.let(::impactOf).orEmpty(),
                attack.note.orEmpty()
            )
        }
        return BYTE_ORDER_MARK +
            (listOf(HEADER) + rows).joinToString(LINE_END, postfix = LINE_END) { row ->
                row.joinToString(separator.toString()) { field(it, separator) }
            }
    }

    private fun time(instant: Instant, zone: ZoneId) = TIME.format(instant.atZone(zone))

    /**
     * A database restored from a phone with newer time-zone data can hold a zone this phone does not know; its times
     * are then written in UTC, and the zone column still names it.
     */
    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }

    private fun field(value: String, separator: Char): String {
        val text = if (value.firstOrNull() in FORMULA_STARTS) "'$value" else value
        val quoted = text.any { it == separator || it == QUOTE || it == '\n' || it == '\r' }
        return if (quoted) QUOTE + text.replace("$QUOTE", "$QUOTE$QUOTE") + QUOTE else text
    }

    private fun levelOf(peak: Int) = when {
        peak <= MILD_UNTIL -> "Mild"
        peak <= MODERATE_UNTIL -> "Moderate"
        else -> "Severe"
    }

    private fun impactOf(impact: Int) = when (impact) {
        IMPACT_NONE -> "No impact"
        IMPACT_LESS_ACTIVE -> "Less active"
        IMPACT_HAD_TO_STOP -> "Had to stop"
        IMPACT_BED_REST -> "Bed rest"
        else -> ""
    }

    private fun symptomOf(tag: AttackTagDataModel, tagNames: Map<String, String>): String? =
        when (tag) {
            is AttackTagDataModel.BuiltInSymptom -> symptomOf(tag.symptom)
            is AttackTagDataModel.CustomSymptom -> tagNames[tag.tagId]
            is AttackTagDataModel.Location,
            is AttackTagDataModel.BuiltInTrigger,
            is AttackTagDataModel.CustomTrigger -> null
        }

    private fun triggerOf(tag: AttackTagDataModel, tagNames: Map<String, String>): String? =
        when (tag) {
            is AttackTagDataModel.BuiltInTrigger -> triggerOf(tag.trigger)
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

    private fun symptomOf(symptom: BuiltInSymptomDataModel) = when (symptom) {
        BuiltInSymptomDataModel.Aura -> "Aura"
        BuiltInSymptomDataModel.Nausea -> "Nausea"
        BuiltInSymptomDataModel.Vomiting -> "Vomiting"
        BuiltInSymptomDataModel.LightSensitivity -> "Sensitivity to light"
        BuiltInSymptomDataModel.SoundSensitivity -> "Sensitivity to sound"
        BuiltInSymptomDataModel.SmellSensitivity -> "Sensitivity to smell"
        BuiltInSymptomDataModel.Dizziness -> "Dizziness"
        BuiltInSymptomDataModel.NeckPain -> "Neck pain"
    }

    private fun triggerOf(trigger: BuiltInTriggerDataModel) = when (trigger) {
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
}
