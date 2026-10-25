package com.migrely.app.insights.domain.usecase

import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import com.migrely.app.patterns.domain.PatternsReadiness
import java.time.LocalDate

/** The last [months] months on [today], today included, as the doctor report counts them (BRD D81, D87). */
internal fun lastMonthsOn(months: Int, today: LocalDate): InsightsDatesDomainModel =
    PatternsReadiness.lastMonths(months, today).let {
        InsightsDatesDomainModel(it.start, it.endInclusive)
    }
