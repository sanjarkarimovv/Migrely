package com.migrely.app.history.domain.model

import java.time.LocalDate

/**
 * One day of a month with every attack that touched it, oldest first (BRD EC-ATK-06, EC-TIM-02). [painFree] holds only
 * when the day is marked pain-free and no attack touches it (BRD EC-DAY-01, D47).
 * [attackMedication] holds when a medication for attacks was taken on the day, in the zone where
 * it was taken; preventive ones never count (BRD D45, D100).
 */
data class HistoryDayDomainModel(
    val date: LocalDate,
    val attacks: List<HistoryAttackDomainModel>,
    val period: Boolean = false,
    val painFree: Boolean = false,
    val attackMedication: Boolean = false
)
