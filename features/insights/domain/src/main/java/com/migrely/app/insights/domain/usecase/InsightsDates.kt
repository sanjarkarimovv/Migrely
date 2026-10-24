package com.migrely.app.insights.domain.usecase

import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import java.time.LocalDate

/** The last [months] months on [today], today included, as the doctor report counts them (BRD D81, D87). */
internal fun lastMonthsOn(months: Int, today: LocalDate): InsightsDatesDomainModel =
    InsightsDatesDomainModel(today.minusMonths(months.toLong()).plusDays(1), today)
