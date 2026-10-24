package com.migrely.app.history.domain.model

import java.time.YearMonth

/**
 * How a month's migraine days compare with the month before (BRD EC-UX-14, D55): [difference] is negative when there
 * were fewer.
 */
data class ProgressDomainModel(val difference: Int, val comparedWith: ComparedWith) {
    sealed interface ComparedWith {
        /** A past month against the whole of [month]. */
        data class WholeMonth(val month: YearMonth) : ComparedWith

        /** The current month, even on its last day, against the same dates of last month. */
        data object SameDaysLastMonth : ComparedWith
    }
}
