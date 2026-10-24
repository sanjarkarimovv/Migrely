package com.migrely.app.report.domain.usecase

import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.report.domain.model.ReportAttackDomainModel
import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportDayLevelDomainModel
import com.migrely.app.report.domain.model.ReportEffectDomainModel
import com.migrely.app.report.domain.model.ReportIntakeDomainModel
import com.migrely.app.report.domain.model.ReportMedicationCategoryDomainModel
import com.migrely.app.report.domain.model.ReportMedicationDomainModel
import com.migrely.app.report.domain.model.ReportMedicationKindDomainModel
import com.migrely.app.report.domain.model.ReportMonthDomainModel
import com.migrely.app.report.domain.model.ReportNoteDomainModel
import com.migrely.app.report.domain.model.ReportPainLevelDomainModel
import com.migrely.app.report.domain.model.ReportRecordsDomainModel
import com.migrely.app.report.domain.model.ReportSummaryDomainModel
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val DAYS_PER_MONTH = 30.0
private const val MILD_UNTIL = 3
private const val MODERATE_UNTIL = 6
private const val BED_REST = 3
private const val CHRONIC_DAYS = 15
private const val SIMPLE_LIMIT = 15
private const val OTHER_LIMIT = 10

/**
 * The report's numbers, by the app's own rules (BRD D40, D45, D57, D82, D83): an attack's days in the zone it was
 * logged in, a medication's days in the zone it was taken in. [now] ends an attack still in progress.
 */
class ReportCalculator(private val dates: ReportDatesDomainModel, private val now: Instant) {
    private val period = dates.from..dates.to
    private val days = ChronoUnit.DAYS.between(dates.from, dates.to).toInt() + 1

    /** The attacks touching the period, newest first. */
    fun attacks(records: ReportRecordsDomainModel): List<ReportAttackDomainModel> =
        records.attacks.filter { touched(it).any(period::contains) }.sortedByDescending { it.start }

    fun anythingLogged(records: ReportRecordsDomainModel): Boolean =
        attacks(records).isNotEmpty() || intakes(records).isNotEmpty() ||
            records.painFreeDates.any(period::contains)

    fun summary(records: ReportRecordsDomainModel): ReportSummaryDomainModel {
        val attacks = attacks(records)
        val started = attacks.filter { it.start.atZone(it.zone).toLocalDate() in period }
        val migraineDates = attacks.flatMap(::touched).filter(period::contains).toSet()
        val acute = intakes(records).filter { it.kind == ReportMedicationKindDomainModel.Acute }
        val medicationDates = acute.map(::dateOf).toSet()
        val months = monthsOfPeriod()
        return ReportSummaryDomainModel(
            days = days,
            migraineDays = migraineDates.size,
            migraineDaysPerMonth = perMonth(migraineDates.size),
            attacksStarted = started.size,
            attackMedicationDays = medicationDates.size,
            attackMedicationDaysPerMonth = perMonth(medicationDates.size),
            overuseMonths = months.filter {
                overused(
                    acute.filter { intake ->
                        YearMonth.from(dateOf(intake)) ==
                            it
                    }
                )
            },
            bedRestAttacks = started.count { it.functionalImpact == BED_REST },
            averagePeak = averageOf(started.mapNotNull { it.peakIntensity }),
            chronic = months.any { month ->
                migraineDates.count { YearMonth.from(it) == month } >=
                    CHRONIC_DAYS
            }
        )
    }

    /** A month of the heat map for each month the period touches. */
    fun months(records: ReportRecordsDomainModel): List<ReportMonthDomainModel> {
        val attacks = attacks(records)
        val levels = mutableMapOf<LocalDate, ReportDayLevelDomainModel>()
        attacks.forEach { attack ->
            val level = levelOf(attack.peakIntensity)
            touched(attack).filter(period::contains).forEach { date ->
                levels[date] = maxOf(levels[date] ?: level, level)
            }
        }
        return monthsOfPeriod().map { month ->
            ReportMonthDomainModel(
                month,
                (1..month.lengthOfMonth()).map { day ->
                    val date = month.atDay(day)
                    when {
                        date !in period -> ReportDayLevelDomainModel.Outside
                        date in levels -> levels.getValue(date)
                        date in records.painFreeDates -> ReportDayLevelDomainModel.Clear
                        else -> ReportDayLevelDomainModel.Nothing
                    }
                }
            )
        }
    }

    /** Each medication taken in the period, the most used first. */
    fun medications(records: ReportRecordsDomainModel): List<ReportMedicationDomainModel> =
        intakes(records).groupBy { it.medicationName }.map { (name, intakes) ->
            ReportMedicationDomainModel(
                name = name,
                kind = intakes.first().kind,
                category = intakes.first().category,
                intakes = intakes.size,
                daysUsed = intakes.map(::dateOf).toSet().size,
                answered = intakes.count { it.effect != null },
                helped = intakes.count {
                    it.effect == ReportEffectDomainModel.ALot ||
                        it.effect == ReportEffectDomainModel.Completely
                }
            )
        }.sortedWith(
            compareByDescending<ReportMedicationDomainModel> {
                it.intakes
            }.thenBy { it.name }
        )

    /** The notes of the period's attacks, newest first. */
    fun notes(records: ReportRecordsDomainModel): List<ReportNoteDomainModel> = attacks(records)
        .mapNotNull { attack ->
            attack.note?.trim()?.takeIf(String::isNotEmpty)
                ?.let { ReportNoteDomainModel(attack.start.atZone(attack.zone).toLocalDate(), it) }
        }

    private fun touched(attack: ReportAttackDomainModel): List<LocalDate> {
        val range = AttackDays.datesTouched(attack.start, attack.zone, attack.end, now)
        return generateSequence(range.start) {
            it.plusDays(1)
        }.takeWhile { it <= range.endInclusive }.toList()
    }

    private fun intakes(records: ReportRecordsDomainModel) = records.intakes.filter {
        dateOf(it) in
            period
    }

    private fun dateOf(intake: ReportIntakeDomainModel): LocalDate =
        intake.takenAt.atZone(intake.zone).toLocalDate()

    private fun monthsOfPeriod(): List<YearMonth> = generateSequence(YearMonth.from(dates.from)) {
        it.plusMonths(1)
    }
        .takeWhile { it <= YearMonth.from(dates.to) }
        .toList()

    private fun perMonth(count: Int) =
        (count * DAYS_PER_MONTH / days).toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble()

    /** One category at its limit, or two or more together on 10 days (BRD D40). "Not sure" counts as other. */
    private fun overused(intakes: List<ReportIntakeDomainModel>): Boolean {
        val byCategory = intakes.groupBy({
            it.category
                ?: ReportMedicationCategoryDomainModel.CombinationOrOther
        }) {
            dateOf(it)
        }.mapValues { (_, dates) -> dates.toSet() }
        val single = byCategory.any { (category, dates) ->
            dates.size >=
                if (category ==
                    ReportMedicationCategoryDomainModel.SimplePainkiller
                ) {
                    SIMPLE_LIMIT
                } else {
                    OTHER_LIMIT
                }
        }
        val together =
            byCategory.size >= 2 && byCategory.values.flatten().toSet().size >= OTHER_LIMIT
        return single || together
    }

    /** The mean of Mild 1, Moderate 2, Severe 3, halves rounded up, as History's month summary does. */
    private fun averageOf(peaks: List<Int>): ReportPainLevelDomainModel? {
        if (peaks.isEmpty()) return null
        val mean = peaks.map { painLevelOf(it).ordinal + 1 }.average()
        val rounded = mean.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toInt()
        return ReportPainLevelDomainModel.entries[rounded - 1]
    }

    private fun painLevelOf(peak: Int) = when {
        peak <= MILD_UNTIL -> ReportPainLevelDomainModel.Mild
        peak <= MODERATE_UNTIL -> ReportPainLevelDomainModel.Moderate
        else -> ReportPainLevelDomainModel.Severe
    }

    /** An attack with no pain level reads as moderate, as the calendar's cloudy sky (BRD EC-UX-09). */
    private fun levelOf(peak: Int?) = when (peak?.let(::painLevelOf)) {
        ReportPainLevelDomainModel.Mild -> ReportDayLevelDomainModel.Mild
        ReportPainLevelDomainModel.Moderate, null -> ReportDayLevelDomainModel.Moderate
        ReportPainLevelDomainModel.Severe -> ReportDayLevelDomainModel.Severe
    }
}
