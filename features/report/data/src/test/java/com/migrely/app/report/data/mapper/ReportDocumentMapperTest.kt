package com.migrely.app.report.data.mapper

import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportDayLevelDataModel
import com.migrely.app.datasource.report.model.ReportPaperDataModel
import com.migrely.app.report.domain.model.ReportAttackDomainModel
import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportDayLevelDomainModel
import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportMedicationCategoryDomainModel
import com.migrely.app.report.domain.model.ReportMedicationDomainModel
import com.migrely.app.report.domain.model.ReportMedicationKindDomainModel
import com.migrely.app.report.domain.model.ReportMonthDomainModel
import com.migrely.app.report.domain.model.ReportNoteDomainModel
import com.migrely.app.report.domain.model.ReportPainLevelDomainModel
import com.migrely.app.report.domain.model.ReportSummaryDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")
private val SUMMARY = ReportSummaryDomainModel(
    days = 92,
    migraineDays = 18,
    migraineDaysPerMonth = 5.9,
    attacksStarted = 4,
    attackMedicationDays = 9,
    attackMedicationDaysPerMonth = 2.9,
    overuseSpans = listOf(
        ReportDatesDomainModel(LocalDate.of(2026, 7, 28), LocalDate.of(2026, 8, 26))
    ),
    bedRestAttacks = 2,
    averagePeak = ReportPainLevelDomainModel.Moderate,
    chronic = false
)

private fun attack(zone: ZoneId, end: Instant?) = ReportAttackDomainModel(
    start = Instant.parse("2026-10-20T04:00:00Z"),
    zone = zone,
    end = end,
    peakIntensity = 7,
    functionalImpact = 3,
    note = null,
    locations = listOf("Left side"),
    symptoms = listOf("Nausea", "Ko‘ngil aynishi"),
    triggers = listOf("Stress"),
    medications = listOf("Ibuprofen 400 mg")
)

private fun report(
    medications: List<ReportMedicationDomainModel> = emptyList(),
    notes: List<ReportNoteDomainModel> = emptyList(),
    attacks: List<ReportAttackDomainModel> =
        listOf(attack(TASHKENT, Instant.parse("2026-10-20T06:15:00Z")))
) = ReportDomainModel(
    dates = ReportDatesDomainModel(LocalDate.of(2026, 7, 26), LocalDate.of(2026, 10, 25)),
    madeOn = LocalDate.of(2026, 10, 25),
    summary = SUMMARY,
    months = listOf(
        ReportMonthDomainModel(
            YearMonth.of(2026, 10),
            List(31) {
                ReportDayLevelDomainModel.Severe
            }
        )
    ),
    attacks = attacks,
    medications = medications,
    notes = notes
)

class ReportDocumentMapperTest {
    private var locale: Locale = Locale.UK
    private var is24Hour = true
    private val classUnderTest = ReportDocumentMapper(
        ReportDateTimeFormatter(
            locale = { locale },
            is24HourFormat = { is24Hour },
            // What Android's DateFormat.getBestDateTimePattern gives in English.
            bestDateTimePattern = { _, skeleton -> if (skeleton == "Hm") "HH:mm" else "h:mm a" }
        )
    )

    private fun blocks(report: ReportDomainModel) =
        classUnderTest.toData(report, ReportPaperDataModel.A4, TASHKENT).blocks

    @Test
    fun `When mapping then the file, paper, footer and page limit`() {
        val document = classUnderTest.toData(report(), ReportPaperDataModel.Letter, TASHKENT)

        assertEquals("migrely-report-2026-10-25.pdf", document.fileName)
        assertEquals(ReportPaperDataModel.Letter, document.paper)
        assertEquals("Patient-entered data. Not a diagnosis.", document.footer)
        assertEquals("Page 2 of 5", document.pageLabel.format(2, 5))
        assertEquals(50, document.maxPages)
    }

    @Test
    fun `When mapping the summary then the doctor's numbers, read in words`() {
        val facts = blocks(report()).filterIsInstance<ReportBlockDataModel.Facts>().single().facts

        assertEquals(
            listOf(
                "5.9 (18 days in all)",
                "4",
                "2.9 (9 days in all)",
                "28 Jul 2026 to 26 Aug 2026",
                "2",
                "Moderate",
                "No"
            ),
            facts.map { it.value }
        )
        assertTrue(
            (blocks(report())[1] as ReportBlockDataModel.Paragraph).text
                .startsWith("26 Jul 2026 to 25 Oct 2026 (92 days).")
        )
    }

    @Test
    fun `When mapping an attack then its time in its own zone, named when not the phone's`() {
        val tables = blocks(
            report(
                attacks = listOf(
                    attack(ZoneId.of("Asia/Tokyo"), null),
                    attack(TASHKENT, Instant.parse("2026-10-20T06:15:00Z"))
                )
            )
        ).filterIsInstance<ReportBlockDataModel.Table>()

        val rows = tables.first().rows
        assertEquals(
            listOf("20 Oct 2026, 13:00 (Asia/Tokyo)", "In progress", "7 · Severe", "Bed rest"),
            rows[0].take(4)
        )
        assertEquals(
            listOf(
                "20 Oct 2026, 09:00",
                "2 h 15 min",
                "7 · Severe",
                "Bed rest",
                "Left side",
                "Nausea, Ko‘ngil aynishi",
                "Stress",
                // A dose's number and unit stay on one line (store polish).
                "Ibuprofen 400${Char(0x00A0)}mg"
            ),
            rows[1]
        )
        assertTrue(
            tables.first().shortenedNote!!.format(
                10,
                900
            ).startsWith("Showing the latest 10 of 900")
        )
    }

    @Test
    fun `Given no medications and no notes then a line instead, and no notes section`() {
        val blocks = blocks(report())

        assertTrue(
            blocks.contains(
                ReportBlockDataModel.Paragraph("No medication was logged in this period.")
            )
        )
        assertTrue(blocks.none { it == ReportBlockDataModel.Heading("Notes") })
        assertEquals(
            ReportDayLevelDataModel.Severe,
            blocks.filterIsInstance<ReportBlockDataModel.HeatMap>().single()
                .months.single().levels.first()
        )
    }

    @Test
    fun `Given a phone in the UK then the calendar's weeks start on Monday`() {
        val heatMap = blocks(report()).filterIsInstance<ReportBlockDataModel.HeatMap>().single()

        assertEquals(listOf("M", "T", "W", "T", "F", "S", "S"), heatMap.weekdays)
        // 1 October 2026 is a Thursday.
        assertEquals(3, heatMap.months.single().leadingEmptyDays)
    }

    @Test
    fun `Given a phone in the US then the calendar's weeks start on Sunday, as the app's do`() {
        locale = Locale.US

        val heatMap = blocks(report()).filterIsInstance<ReportBlockDataModel.HeatMap>().single()

        assertEquals(listOf("S", "M", "T", "W", "T", "F", "S"), heatMap.weekdays)
        assertEquals(4, heatMap.months.single().leadingEmptyDays)
    }

    @Test
    fun `Given medications and notes then their tables`() {
        val tables = blocks(
            report(
                medications = listOf(
                    ReportMedicationDomainModel(
                        "Ibuprofen",
                        ReportMedicationKindDomainModel.Acute,
                        ReportMedicationCategoryDomainModel.SimplePainkiller,
                        3,
                        2,
                        3,
                        2
                    ),
                    ReportMedicationDomainModel(
                        "Topiramate",
                        ReportMedicationKindDomainModel.Preventive,
                        null,
                        1,
                        1,
                        0,
                        0
                    )
                ),
                notes = listOf(ReportNoteDomainModel(LocalDate.of(2026, 10, 7), "Stress at work"))
            )
        ).filterIsInstance<ReportBlockDataModel.Table>()

        assertEquals(
            listOf(
                listOf("Ibuprofen", "Simple painkiller", "3", "2", "2 of 3 answers"),
                listOf("Topiramate", "Preventive", "1", "1", "Not asked")
            ),
            tables[1].rows
        )
        assertEquals(listOf(listOf("7 Oct 2026", "Stress at work")), tables[2].rows)
    }

    @Test
    fun `Given a period under four weeks then totals, not a month's average`() {
        val short = report().copy(summary = SUMMARY.copy(days = 10))

        val facts = blocks(short).filterIsInstance<ReportBlockDataModel.Facts>().single().facts

        assertEquals("Migraine days" to "18 days in all", facts[0].label to facts[0].value)
        assertEquals(
            "Days with attack medication" to "9 days in all",
            facts[2].label to facts[2].value
        )
    }

    @Test
    fun `Given no migraine in the period then a line instead of an empty table`() {
        val blocks = blocks(report(attacks = emptyList()))

        assertTrue(
            blocks.contains(
                ReportBlockDataModel.Paragraph("No migraine was logged in this period.")
            )
        )
        assertTrue(
            blocks.none {
                it is ReportBlockDataModel.Table &&
                    it.columns.first().title == "Start"
            }
        )
    }

    @Test
    fun `When mapping migraines then Peak has room for its level beside the number`() {
        val table = blocks(report()).filterIsInstance<ReportBlockDataModel.Table>().first()

        // Peak's 1.2 of 10.2 is 60.6 pt of A4's 515; "4 · Moderate" takes about 48 pt at 8.5 pt in Roboto.
        assertEquals(
            listOf(
                "Start" to 1.5f,
                "Duration" to 1f,
                "Peak" to 1.2f,
                "Impact" to 1f,
                "Where it hurts" to 1.3f,
                "Symptoms" to 1.4f,
                "Triggers" to 1.4f,
                "Medications" to 1.4f
            ),
            table.columns.map { it.title to it.weight }
        )
    }

    @Test
    fun `Given a 12-hour phone in the US then the migraine's time as the app shows it`() {
        locale = Locale.US
        is24Hour = false

        val rows = blocks(report()).filterIsInstance<ReportBlockDataModel.Table>().first().rows

        assertEquals("20 Oct 2026, 9:00\u00A0AM", rows.single().first())
    }

    @Test
    fun `Given a migraine that began before the period then its row says so`() {
        val before = attack(TASHKENT, null).copy(start = Instant.parse("2026-07-25T18:00:00Z"))

        val rows = blocks(
            report(attacks = listOf(before))
        ).filterIsInstance<ReportBlockDataModel.Table>().first().rows

        assertEquals("25 Jul 2026, 23:00, began before the period", rows.single().first())
    }

    @Test
    fun `Given a zone this phone does not know then the row names it as stored, in UTC`() {
        val unknown = attack(ZoneOffset.UTC, null).copy(unknownZoneName = "Mars/Olympus")

        val rows = blocks(
            report(attacks = listOf(unknown))
        ).filterIsInstance<ReportBlockDataModel.Table>().first().rows

        assertEquals("20 Oct 2026, 04:00 (Mars/Olympus, shown in UTC)", rows.single().first())
    }
}
