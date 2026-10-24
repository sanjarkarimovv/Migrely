package com.migrely.app.medication.domain.usecase

import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.medication.domain.model.AcuteIntakeDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import java.time.Instant
import java.time.LocalDate

private const val WINDOW_DAYS = 30L

private val SINGLE_CATEGORIES = listOf(
    OveruseCategoryDomainModel.SimplePainkiller,
    OveruseCategoryDomainModel.Triptan,
    OveruseCategoryDomainModel.CombinationOrOther
)

/**
 * The days counted towards medication overuse (BRD D45, MED-F5), the one rule for home's warning and History's standing
 * indicator (D59). The window is today, by the device's date, and the 29 days before it. Each intake counts on its date
 * in the zone it was taken in, and a day counts once however many intakes it has. A category is counted only when it
 * has an intake; [OveruseCategoryDomainModel.Multiple] counts every attack-medication day when two or more categories
 * were used (ICHD-3 8.2.6).
 */
internal class OveruseDayCounter {
    /** Where to start reading intakes, with the zone margin before the window. */
    fun readFrom(now: CurrentTimeDomainModel): Instant =
        AttackDays.windowStart(firstDay(now), now.zone)

    /** Where to stop reading intakes, with the zone margin after today. */
    fun readTo(now: CurrentTimeDomainModel): Instant = AttackDays.windowEnd(today(now), now.zone)

    fun days(
        intakes: List<AcuteIntakeDomainModel>,
        now: CurrentTimeDomainModel
    ): Map<OveruseCategoryDomainModel, Int> {
        val window = firstDay(now)..today(now)
        val dates = intakes
            .map { it.overuseCategory() to it.takenAt.atZone(it.zone).toLocalDate() }
            .filter { (_, date) -> date in window }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, dates) -> dates.toSet() }
        val singles = SINGLE_CATEGORIES
            .mapNotNull { category -> dates[category]?.let { category to it.size } }
        val multiple = if (dates.size >= 2) {
            listOf(OveruseCategoryDomainModel.Multiple to dates.values.flatten().toSet().size)
        } else {
            emptyList()
        }
        return (singles + multiple).toMap()
    }

    private fun today(now: CurrentTimeDomainModel): LocalDate =
        now.instant.atZone(now.zone).toLocalDate()

    private fun firstDay(now: CurrentTimeDomainModel): LocalDate =
        today(now).minusDays(WINDOW_DAYS - 1)

    private fun AcuteIntakeDomainModel.overuseCategory() = when (category) {
        MedicationCategoryDomainModel.SimplePainkiller ->
            OveruseCategoryDomainModel.SimplePainkiller

        MedicationCategoryDomainModel.Triptan -> OveruseCategoryDomainModel.Triptan
        MedicationCategoryDomainModel.CombinationOrOther,
        null -> OveruseCategoryDomainModel.CombinationOrOther
    }
}
