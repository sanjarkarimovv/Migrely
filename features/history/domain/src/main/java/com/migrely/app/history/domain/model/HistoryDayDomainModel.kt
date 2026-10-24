package com.migrely.app.history.domain.model

import java.time.LocalDate

/** One day of a month with every attack that touched it, oldest first (BRD EC-ATK-06, EC-TIM-02). */
data class HistoryDayDomainModel(val date: LocalDate, val attacks: List<HistoryAttackDomainModel>)
