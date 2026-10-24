package com.migrely.app.history.domain.model

import java.time.LocalDate

/**
 * One day of a month with every attack that touched it, oldest first (BRD EC-ATK-06, EC-TIM-02). [painFree] holds only
 * when the day is marked pain-free and no attack touches it (BRD EC-DAY-01, D47).
 */
data class HistoryDayDomainModel(
    val date: LocalDate,
    val attacks: List<HistoryAttackDomainModel>,
    val period: Boolean = false,
    val painFree: Boolean = false
)
