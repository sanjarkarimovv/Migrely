package com.migrely.app.report.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** The period a report covers (BRD REP-F1, D81). */
sealed interface ReportPeriodDomainModel {
    /** The last [months] months, today included. */
    data class LastMonths(val months: Int) : ReportPeriodDomainModel

    /** A chosen range; a part after today is left out. */
    data class Range(val from: LocalDate, val to: LocalDate) : ReportPeriodDomainModel
}

data class ReportDatesDomainModel(val from: LocalDate, val to: LocalDate)

data class CurrentTimeDomainModel(val instant: Instant, val zone: ZoneId)

data class CreateReportRequestDomainModel(
    val period: ReportPeriodDomainModel,
    val includeNotes: Boolean
)

/**
 * Every record a report may use. Display words (where it hurts, symptoms, triggers, the medications taken) arrive
 * ready, since the report only lists them.
 */
data class ReportRecordsDomainModel(
    val attacks: List<ReportAttackDomainModel>,
    val intakes: List<ReportIntakeDomainModel>,
    val painFreeDates: Set<LocalDate>
)

data class ReportAttackDomainModel(
    val start: Instant,
    val zone: ZoneId,
    val end: Instant?,
    val peakIntensity: Int?,
    val functionalImpact: Int?,
    val note: String?,
    val locations: List<String>,
    val symptoms: List<String>,
    val triggers: List<String>,
    val medications: List<String>
)

data class ReportIntakeDomainModel(
    val medicationName: String,
    val kind: ReportMedicationKindDomainModel,
    val category: ReportMedicationCategoryDomainModel?,
    val takenAt: Instant,
    val zone: ZoneId,
    val effect: ReportEffectDomainModel?
)

enum class ReportMedicationKindDomainModel {
    Acute,
    Preventive
}

/** Null on an intake means "Not sure", which counts as combination or other for overuse (BRD D45). */
enum class ReportMedicationCategoryDomainModel {
    SimplePainkiller,
    Triptan,
    CombinationOrOther
}

enum class ReportEffectDomainModel {
    NotAtAll,
    ALittle,
    ALot,
    Completely
}

enum class ReportPainLevelDomainModel {
    Mild,
    Moderate,
    Severe
}

/** A day of the heat map, light to dark (BRD D82). */
enum class ReportDayLevelDomainModel {
    Outside,
    Nothing,
    Clear,
    Mild,
    Moderate,
    Severe
}

/** What the report says (BRD REP-F2, D82). [notes] is empty unless the user chose to include them. */
data class ReportDomainModel(
    val dates: ReportDatesDomainModel,
    val madeOn: LocalDate,
    val summary: ReportSummaryDomainModel,
    val months: List<ReportMonthDomainModel>,
    val attacks: List<ReportAttackDomainModel>,
    val medications: List<ReportMedicationDomainModel>,
    val notes: List<ReportNoteDomainModel>
)

/**
 * The first page. A month's average is the count times 30 over the period's days (D83). [overuseSpans] are the 30-day
 * spans, back from the period's last day, in which some category reached its limit, as home's card counts them (D45);
 * [chronic] is a calendar month the period touches with 15 or more migraine days, as History counts it.
 */
data class ReportSummaryDomainModel(
    val days: Int,
    val migraineDays: Int,
    val migraineDaysPerMonth: Double,
    val attacksStarted: Int,
    val attackMedicationDays: Int,
    val attackMedicationDaysPerMonth: Double,
    val overuseSpans: List<ReportDatesDomainModel>,
    val bedRestAttacks: Int,
    val averagePeak: ReportPainLevelDomainModel?,
    val chronic: Boolean
)

/** One month of the heat map: a level for each of its dates. */
data class ReportMonthDomainModel(val month: YearMonth, val levels: List<ReportDayLevelDomainModel>)

/** [helped] counts the answers "A lot" and "Completely" among the [answered] ones. */
data class ReportMedicationDomainModel(
    val name: String,
    val kind: ReportMedicationKindDomainModel,
    val category: ReportMedicationCategoryDomainModel?,
    val intakes: Int,
    val daysUsed: Int,
    val answered: Int,
    val helped: Int
)

data class ReportNoteDomainModel(val date: LocalDate, val note: String)

sealed interface ReportRecordsResultDomainModel {
    data class Loaded(val records: ReportRecordsDomainModel) : ReportRecordsResultDomainModel

    data object Failed : ReportRecordsResultDomainModel
}

sealed interface ReportOptionsDomainModel {
    /** [anythingLogged] is false when the period holds no attack, intake or pain-free mark (BRD EC-REP-01). */
    data class Ready(val dates: ReportDatesDomainModel, val anythingLogged: Boolean) :
        ReportOptionsDomainModel

    data object Failed : ReportOptionsDomainModel
}

sealed interface ReportWriteResultDomainModel {
    /** [uri] is a content URI another app may be given to read the file. */
    data class Written(val uri: String) : ReportWriteResultDomainModel

    data object StorageFull : ReportWriteResultDomainModel

    data object Failed : ReportWriteResultDomainModel
}

sealed interface CreateReportResultDomainModel {
    data class Created(val uri: String) : CreateReportResultDomainModel

    data object NothingLogged : CreateReportResultDomainModel

    data object StorageFull : CreateReportResultDomainModel

    data object Failed : CreateReportResultDomainModel
}
