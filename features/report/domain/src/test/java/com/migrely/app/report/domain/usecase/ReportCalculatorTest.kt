package com.migrely.app.report.domain.usecase

import com.migrely.app.report.domain.model.ReportAttackDomainModel
import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportDayLevelDomainModel
import com.migrely.app.report.domain.model.ReportEffectDomainModel
import com.migrely.app.report.domain.model.ReportIntakeDomainModel
import com.migrely.app.report.domain.model.ReportMedicationCategoryDomainModel
import com.migrely.app.report.domain.model.ReportMedicationDomainModel
import com.migrely.app.report.domain.model.ReportMedicationKindDomainModel
import com.migrely.app.report.domain.model.ReportNoteDomainModel
import com.migrely.app.report.domain.model.ReportPainLevelDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.domain.model.ReportRecordsDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")
private val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
private val TODAY: LocalDate = LocalDate.of(2026, 10, 25)
private val NOW: Instant = TODAY.atTime(12, 0).atZone(TASHKENT).toInstant()
private val SIMPLE = ReportMedicationCategoryDomainModel.SimplePainkiller
private val THREE_MONTHS = ReportPeriodDomainModel.LastMonths(3).datesOn(TODAY)

private fun at(text: String, zone: ZoneId = TASHKENT): Instant =
    LocalDateTime.parse(text).atZone(zone).toInstant()

private fun attack(
    start: String,
    hours: Long? = 2,
    peak: Int? = 5,
    impact: Int? = null,
    note: String? = null,
    zone: ZoneId = TASHKENT
) = ReportAttackDomainModel(
    start = at(start, zone),
    zone = zone,
    end = hours?.let { at(start, zone).plusSeconds(it * 3_600) },
    peakIntensity = peak,
    functionalImpact = impact,
    note = note,
    locations = emptyList(),
    symptoms = emptyList(),
    triggers = emptyList(),
    medications = emptyList()
)

private fun intake(
    taken: String,
    category: ReportMedicationCategoryDomainModel? = SIMPLE,
    name: String = "Ibuprofen",
    kind: ReportMedicationKindDomainModel = ReportMedicationKindDomainModel.Acute,
    effect: ReportEffectDomainModel? = null
) = ReportIntakeDomainModel(name, kind, category, at(taken), TASHKENT, effect)

private fun records(
    attacks: List<ReportAttackDomainModel> = emptyList(),
    intakes: List<ReportIntakeDomainModel> = emptyList(),
    painFree: Set<LocalDate> = emptySet()
) = ReportRecordsDomainModel(attacks, intakes, painFree)

class ReportCalculatorTest {
    private val classUnderTest = ReportCalculator(THREE_MONTHS, NOW)

    @Test
    fun `The last three months are 92 days, today included, and a range never passes today`() {
        assertEquals(ReportDatesDomainModel(LocalDate.of(2026, 7, 26), TODAY), THREE_MONTHS)
        assertEquals(
            ReportDatesDomainModel(LocalDate.of(2026, 10, 1), TODAY),
            ReportPeriodDomainModel.Range(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 12, 1)
            ).datesOn(TODAY)
        )
        assertEquals(
            ReportDatesDomainModel(TODAY, TODAY),
            ReportPeriodDomainModel.Range(
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 12, 1)
            ).datesOn(TODAY)
        )
    }

    @Test
    fun `Given 18 migraine days in 92 days then 5,9 a month`() {
        val attacks = (1..18).map { attack("2026-08-%02dT09:00".format(it)) }

        val summary = classUnderTest.summary(records(attacks))

        assertEquals(92, summary.days)
        assertEquals(18, summary.migraineDays)
        assertEquals(5.9, summary.migraineDaysPerMonth, 0.0)
        assertEquals(18, summary.attacksStarted)
    }

    @Test
    fun `Given an attack from before the period then its days in it count, but not the start`() {
        val summary = classUnderTest.summary(
            records(listOf(attack("2026-07-25T20:00", hours = 20)))
        )

        assertEquals(1, summary.migraineDays)
        assertEquals(0, summary.attacksStarted)
    }

    @Test
    fun `Given an attack in progress then it touches every day up to now`() {
        val summary = classUnderTest.summary(
            records(listOf(attack("2026-10-23T09:00", hours = null)))
        )

        assertEquals(3, summary.migraineDays)
    }

    @Test
    fun `Given an attack logged in Tokyo then its day is Tokyo's`() {
        val months = classUnderTest.months(
            records(listOf(attack("2026-10-15T01:00", zone = TOKYO)))
        )

        val october = months.single { it.month == YearMonth.of(2026, 10) }.levels
        assertEquals(ReportDayLevelDomainModel.Moderate, october[14])
        assertEquals(ReportDayLevelDomainModel.Nothing, october[13])
    }

    @Test
    fun `Bed rest, the average peak with halves up, and chronic at 15 days in a month`() {
        val attacks =
            (1..15).map {
                attack(
                    "2026-09-%02dT09:00".format(it),
                    peak = if (it ==
                        1
                    ) {
                        2
                    } else {
                        9
                    },
                    impact = 3
                )
            } +
                attack("2026-10-01T09:00", peak = null)
        val summary = classUnderTest.summary(records(attacks))

        assertEquals(15, summary.bedRestAttacks)
        assertEquals(ReportPainLevelDomainModel.Severe, summary.averagePeak)
        assertTrue(summary.chronic)
        assertFalse(classUnderTest.summary(records(attacks.drop(1))).chronic)
        assertEquals(
            ReportPainLevelDomainModel.Severe,
            classUnderTest.summary(
                records(
                    listOf(
                        attack("2026-09-01T09:00", peak = 5),
                        attack("2026-09-03T09:00", peak = 9)
                    )
                )
            )
                .averagePeak
        )
    }

    @Test
    fun `Overuse counts per calendar month by each category's limit, and two together`() {
        val simple = (1..15).map { intake("2026-08-%02dT09:00".format(it)) }
        val triptan = (1..9).map {
            intake(
                "2026-09-%02dT09:00".format(it),
                ReportMedicationCategoryDomainModel.Triptan,
                "Sumatriptan"
            )
        }
        val together = (1..5).map { intake("2026-10-%02dT09:00".format(it)) } +
            (6..10).map { intake("2026-10-%02dT09:00".format(it), null, "Combination") }

        val summary = classUnderTest.summary(records(intakes = simple + triptan + together))

        assertEquals(listOf(YearMonth.of(2026, 8), YearMonth.of(2026, 10)), summary.overuseMonths)
        assertEquals(34, summary.attackMedicationDays)
    }

    @Test
    fun `Preventive intakes are not attack medication`() {
        val summary = classUnderTest.summary(
            records(
                intakes = listOf(
                    intake(
                        "2026-10-01T09:00",
                        null,
                        "Topiramate",
                        ReportMedicationKindDomainModel.Preventive
                    )
                )
            )
        )

        assertEquals(0, summary.attackMedicationDays)
    }

    @Test
    fun `The heat map shows each day's strongest attack, pain-free days and days outside`() {
        val months = classUnderTest.months(
            records(
                attacks = listOf(
                    attack("2026-10-10T09:00", peak = 2),
                    attack("2026-10-10T15:00", peak = 8)
                ),
                painFree = setOf(LocalDate.of(2026, 10, 11))
            )
        )

        assertEquals(
            listOf(
                YearMonth.of(2026, 7),
                YearMonth.of(2026, 8),
                YearMonth.of(2026, 9),
                YearMonth.of(2026, 10)
            ),
            months.map {
                it.month
            }
        )
        val july = months.first().levels
        assertEquals(ReportDayLevelDomainModel.Outside, july[24])
        assertEquals(ReportDayLevelDomainModel.Nothing, july[25])
        val october = months.last().levels
        assertEquals(ReportDayLevelDomainModel.Severe, october[9])
        assertEquals(ReportDayLevelDomainModel.Clear, october[10])
        assertEquals(ReportDayLevelDomainModel.Outside, october[25])
    }

    @Test
    fun `Medications list intakes, days used and how they helped, the most used first`() {
        val medications = classUnderTest.medications(
            records(
                intakes = listOf(
                    intake("2026-10-01T09:00", effect = ReportEffectDomainModel.ALot),
                    intake("2026-10-01T18:00", effect = ReportEffectDomainModel.NotAtAll),
                    intake("2026-10-02T09:00", effect = ReportEffectDomainModel.Completely),
                    intake(
                        "2026-10-03T09:00",
                        ReportMedicationCategoryDomainModel.Triptan,
                        "Sumatriptan"
                    ),
                    intake("2026-06-01T09:00")
                )
            )
        )

        assertEquals(
            listOf(
                ReportMedicationDomainModel(
                    "Ibuprofen",
                    ReportMedicationKindDomainModel.Acute,
                    ReportMedicationCategoryDomainModel.SimplePainkiller,
                    intakes = 3,
                    daysUsed = 2,
                    answered = 3,
                    helped = 2
                ),
                ReportMedicationDomainModel(
                    "Sumatriptan",
                    ReportMedicationKindDomainModel.Acute,
                    ReportMedicationCategoryDomainModel.Triptan,
                    intakes = 1,
                    daysUsed = 1,
                    answered = 0,
                    helped = 0
                )
            ),
            medications
        )
    }

    @Test
    fun `Notes are the period's, trimmed, newest first, and blank ones are left out`() {
        val notes = classUnderTest.notes(
            records(
                listOf(
                    attack("2026-10-01T09:00", note = "  Little sleep "),
                    attack("2026-10-05T09:00", note = " "),
                    attack("2026-10-07T09:00", note = "Stress at work"),
                    attack("2026-05-07T09:00", note = "Too old")
                )
            )
        )

        assertEquals(
            listOf(
                ReportNoteDomainModel(LocalDate.of(2026, 10, 7), "Stress at work"),
                ReportNoteDomainModel(LocalDate.of(2026, 10, 1), "Little sleep")
            ),
            notes
        )
    }

    @Test
    fun `Anything logged means an attack, an intake or a pain-free mark in the period`() {
        assertFalse(classUnderTest.anythingLogged(records(listOf(attack("2026-05-01T09:00")))))
        assertTrue(
            classUnderTest.anythingLogged(records(intakes = listOf(intake("2026-10-01T09:00"))))
        )
        assertTrue(classUnderTest.anythingLogged(records(painFree = setOf(TODAY))))
    }
}
