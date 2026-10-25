package com.migrely.app.report.data.mapper

import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportColumnDataModel
import com.migrely.app.datasource.report.model.ReportDayLevelDataModel
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import com.migrely.app.datasource.report.model.ReportFactDataModel
import com.migrely.app.datasource.report.model.ReportHeatMonthDataModel
import com.migrely.app.datasource.report.model.ReportLegendDataModel
import com.migrely.app.datasource.report.model.ReportPaperDataModel
import com.migrely.app.report.domain.model.ReportAttackDomainModel
import com.migrely.app.report.domain.model.ReportDayLevelDomainModel
import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportMedicationCategoryDomainModel
import com.migrely.app.report.domain.model.ReportMedicationDomainModel
import com.migrely.app.report.domain.model.ReportMedicationKindDomainModel
import com.migrely.app.report.domain.model.ReportPainLevelDomainModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** At most this many pages; a longer attacks table keeps its latest rows (BRD EC-REP-02, D84). */
const val MAXIMUM_REPORT_PAGES = 50

private const val SHORTENED_NOTE =
    "Showing the latest %1\$d of %2\$d migraines. The summary counts all of them."
private const val SHORTENED_NOTES = "Showing the latest %1\$d of %2\$d notes."
private const val AVERAGE_FROM_DAYS = 28
private const val MILD_UNTIL = 3
private const val MODERATE_UNTIL = 6
private const val MINUTES_PER_HOUR = 60
private const val IMPACT_NONE = 0
private const val IMPACT_LESS_ACTIVE = 1
private const val IMPACT_HAD_TO_STOP = 2
private const val IMPACT_BED_REST = 3
private const val DAYS_PER_WEEK = 7
private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMM yyyy, HH:mm",
    Locale.ENGLISH
)
private val MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

/**
 * The words of the report (BRD REP-F2, REP-F3, D82). It is a document for a doctor, so its words are fixed English,
 * the app's language, and the user's own words stay as typed. A time is shown in the zone it was logged in, which is
 * named when it is not the phone's [deviceZone]. Weeks follow the phone, as in the app (EC-TIM-05).
 */
class ReportDocumentMapper(private val reportDateTimeFormatter: ReportDateTimeFormatter) {
    fun toData(report: ReportDomainModel, paper: ReportPaperDataModel, deviceZone: ZoneId) =
        ReportDocumentDataModel(
            fileName = "migrely-report-${report.madeOn}.pdf",
            paper = paper,
            footer = "Patient-entered data. Not a diagnosis.",
            pageLabel = "Page %1\$d of %2\$d",
            maxPages = MAXIMUM_REPORT_PAGES,
            blocks =
            summary(report) + calendar(report) + attacks(report, deviceZone) + medications(report) +
                notes(report)
        )

    private fun summary(report: ReportDomainModel): List<ReportBlockDataModel> {
        val summary = report.summary
        return listOf(
            ReportBlockDataModel.Title("Migraine diary report"),
            ReportBlockDataModel.Paragraph(
                "${DATE.format(
                    report.dates.from
                )} to ${DATE.format(report.dates.to)} (${summary.days} days). " +
                    "Made with Migrely on ${DATE.format(report.madeOn)}."
            ),
            ReportBlockDataModel.Heading("Summary"),
            ReportBlockDataModel.Facts(
                listOf(
                    ReportFactDataModel(
                        if (summary.days <
                            AVERAGE_FROM_DAYS
                        ) {
                            "Migraine days"
                        } else {
                            "Migraine days a month, on average"
                        },
                        averageOf(summary.migraineDaysPerMonth, summary.migraineDays, summary.days)
                    ),
                    ReportFactDataModel("Migraines started", summary.attacksStarted.toString()),
                    ReportFactDataModel(
                        if (summary.days < AVERAGE_FROM_DAYS) {
                            "Days with attack medication"
                        } else {
                            "Days with attack medication a month, on average"
                        },
                        averageOf(
                            summary.attackMedicationDaysPerMonth,
                            summary.attackMedicationDays,
                            summary.days
                        )
                    ),
                    ReportFactDataModel(
                        "30-day spans at the medication-overuse limit",
                        summary.overuseSpans.joinToString(", ") {
                            "${DATE.format(it.from)} to ${DATE.format(it.to)}"
                        }.ifEmpty { "None" }
                    ),
                    ReportFactDataModel(
                        "Migraines with bed rest",
                        summary.bedRestAttacks.toString()
                    ),
                    ReportFactDataModel(
                        "Average peak pain",
                        summary.averagePeak?.let(::levelName) ?: "Not logged"
                    ),
                    ReportFactDataModel(
                        "A month with 15 or more migraine days",
                        if (summary.chronic) "Yes" else "No"
                    )
                )
            )
        )
    }

    /** A period under four weeks gives no fair month's average, so only its total (review). */
    private fun averageOf(perMonth: Double, total: Int, days: Int) =
        if (days < AVERAGE_FROM_DAYS) "$total days in all" else "$perMonth ($total days in all)"

    private fun calendar(report: ReportDomainModel): List<ReportBlockDataModel> = listOf(
        ReportBlockDataModel.Heading("Calendar"),
        heatMap(report, reportDateTimeFormatter.firstDayOfWeek())
    )

    /** Weeks start on the phone's first day, as the app's month calendar does, under English initials. */
    private fun heatMap(report: ReportDomainModel, firstDay: DayOfWeek) =
        ReportBlockDataModel.HeatMap(
            weekdays = (0 until DAYS_PER_WEEK).map {
                firstDay.plus(it.toLong()).getDisplayName(TextStyle.NARROW, Locale.ENGLISH)
            },
            months = report.months.map { month ->
                ReportHeatMonthDataModel(
                    title = MONTH.format(month.month),
                    leadingEmptyDays = Math.floorMod(
                        month.month.atDay(1).dayOfWeek.value - firstDay.value,
                        DAYS_PER_WEEK
                    ),
                    levels = month.levels.map(::levelOf)
                )
            },
            legend = listOf(
                ReportLegendDataModel(ReportDayLevelDataModel.Nothing, "Nothing logged"),
                ReportLegendDataModel(ReportDayLevelDataModel.Clear, "Pain-free"),
                ReportLegendDataModel(ReportDayLevelDataModel.Mild, "Mild, 1–3"),
                ReportLegendDataModel(
                    ReportDayLevelDataModel.Moderate,
                    "Moderate, 4–6, or not logged"
                ),
                ReportLegendDataModel(ReportDayLevelDataModel.Severe, "Severe, 7–10")
            )
        )

    private fun attacks(report: ReportDomainModel, deviceZone: ZoneId): List<ReportBlockDataModel> =
        listOf(
            ReportBlockDataModel.PageBreak,
            ReportBlockDataModel.Heading("Migraines"),
            if (report.attacks.isEmpty()) {
                ReportBlockDataModel.Paragraph("No migraine was logged in this period.")
            } else {
                attacksTable(report, deviceZone)
            }
        )

    private fun attacksTable(report: ReportDomainModel, deviceZone: ZoneId) =
        ReportBlockDataModel.Table(
            columns = listOf(
                ReportColumnDataModel("Start", 1.5f),
                ReportColumnDataModel("Duration", 1f),
                ReportColumnDataModel("Peak", 0.9f),
                ReportColumnDataModel("Impact", 1f),
                ReportColumnDataModel("Where it hurts", 1.3f),
                ReportColumnDataModel("Symptoms", 1.5f),
                ReportColumnDataModel("Triggers", 1.5f),
                ReportColumnDataModel("Medications", 1.5f)
            ),
            rows = report.attacks.map { row(it, deviceZone, report.dates.from) },
            shortenedNote = SHORTENED_NOTE
        )

    private fun row(
        attack: ReportAttackDomainModel,
        deviceZone: ZoneId,
        from: LocalDate
    ): List<String> {
        val began = attack.start.atZone(attack.zone)
        // Listed for its days in the period, but not counted as started in it (walk B4).
        val start = DATE_TIME.format(began) +
            zoneLabel(attack, deviceZone) +
            if (began.toLocalDate().isBefore(from)) ", began before the period" else ""
        val duration =
            attack.end?.let { durationOf(Duration.between(attack.start, it)) } ?: "In progress"
        return listOf(
            start,
            duration,
            attack.peakIntensity?.let { "$it · ${levelName(painLevelOf(it))}" } ?: "Not logged",
            attack.functionalImpact?.let(::impactOf).orEmpty(),
            attack.locations.joinToString(", "),
            attack.symptoms.joinToString(", "),
            attack.triggers.joinToString(", "),
            attack.medications.joinToString(", ")
        )
    }

    /** The zone is named when it is not the phone's; one this phone does not know is named as stored. */
    private fun zoneLabel(attack: ReportAttackDomainModel, deviceZone: ZoneId) = when {
        attack.unknownZoneName.isNotEmpty() -> " (${attack.unknownZoneName}, shown in UTC)"
        attack.zone != deviceZone -> " (${attack.zone.id})"
        else -> ""
    }

    private fun medications(report: ReportDomainModel): List<ReportBlockDataModel> = listOf(
        ReportBlockDataModel.Heading("Medications")
    ) + if (report.medications.isEmpty()) {
        listOf(ReportBlockDataModel.Paragraph("No medication was logged in this period."))
    } else {
        listOf(
            ReportBlockDataModel.Table(
                columns = listOf(
                    ReportColumnDataModel("Medication", 2f),
                    ReportColumnDataModel("Type", 1.6f),
                    ReportColumnDataModel("Intakes", 0.8f),
                    ReportColumnDataModel("Days used", 0.8f),
                    ReportColumnDataModel("Helped a lot or completely", 1.6f)
                ),
                rows = report.medications.map(::row)
            )
        )
    }

    private fun row(medication: ReportMedicationDomainModel) = listOf(
        medication.name,
        typeOf(medication),
        medication.intakes.toString(),
        medication.daysUsed.toString(),
        if (medication.answered ==
            0
        ) {
            "Not asked"
        } else {
            "${medication.helped} of ${medication.answered} answers"
        }
    )

    private fun notes(report: ReportDomainModel): List<ReportBlockDataModel> =
        if (report.notes.isEmpty()) {
            emptyList()
        } else {
            listOf(
                ReportBlockDataModel.Heading("Notes"),
                ReportBlockDataModel.Table(
                    columns = listOf(
                        ReportColumnDataModel("Date", 1f),
                        ReportColumnDataModel("Note", 5f)
                    ),
                    rows = report.notes.map { listOf(DATE.format(it.date), it.note) },
                    shortenedNote = SHORTENED_NOTES
                )
            )
        }

    private fun typeOf(medication: ReportMedicationDomainModel) = when (medication.kind) {
        ReportMedicationKindDomainModel.Preventive -> "Preventive"
        ReportMedicationKindDomainModel.Acute -> when (medication.category) {
            ReportMedicationCategoryDomainModel.SimplePainkiller -> "Simple painkiller"
            ReportMedicationCategoryDomainModel.Triptan -> "Triptan"
            ReportMedicationCategoryDomainModel.CombinationOrOther -> "Combination or other"
            null -> "For attacks, type not sure"
        }
    }

    private fun durationOf(duration: Duration): String {
        val minutes = duration.toMinutes()
        val hours = minutes / MINUTES_PER_HOUR
        return if (hours == 0L) "$minutes min" else "$hours h ${minutes % MINUTES_PER_HOUR} min"
    }

    private fun painLevelOf(peak: Int) = when {
        peak <= MILD_UNTIL -> ReportPainLevelDomainModel.Mild
        peak <= MODERATE_UNTIL -> ReportPainLevelDomainModel.Moderate
        else -> ReportPainLevelDomainModel.Severe
    }

    private fun levelName(level: ReportPainLevelDomainModel) = when (level) {
        ReportPainLevelDomainModel.Mild -> "Mild"
        ReportPainLevelDomainModel.Moderate -> "Moderate"
        ReportPainLevelDomainModel.Severe -> "Severe"
    }

    private fun impactOf(impact: Int) = when (impact) {
        IMPACT_NONE -> "No impact"
        IMPACT_LESS_ACTIVE -> "Less active"
        IMPACT_HAD_TO_STOP -> "Had to stop"
        IMPACT_BED_REST -> "Bed rest"
        else -> ""
    }

    private fun levelOf(level: ReportDayLevelDomainModel) = when (level) {
        ReportDayLevelDomainModel.Outside -> ReportDayLevelDataModel.Outside
        ReportDayLevelDomainModel.Nothing -> ReportDayLevelDataModel.Nothing
        ReportDayLevelDomainModel.Clear -> ReportDayLevelDataModel.Clear
        ReportDayLevelDomainModel.Mild -> ReportDayLevelDataModel.Mild
        ReportDayLevelDomainModel.Moderate -> ReportDayLevelDataModel.Moderate
        ReportDayLevelDomainModel.Severe -> ReportDayLevelDataModel.Severe
    }
}
