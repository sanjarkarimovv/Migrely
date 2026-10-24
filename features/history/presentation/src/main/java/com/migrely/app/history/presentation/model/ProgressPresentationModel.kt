package com.migrely.app.history.presentation.model

import java.time.YearMonth

/** How a month's migraine days compare with the month before (BRD EC-UX-14, D55). */
sealed interface ProgressPresentationModel {
    val comparedWith: ComparedWith

    data class Fewer(val days: Int, override val comparedWith: ComparedWith) :
        ProgressPresentationModel

    data class More(val days: Int, override val comparedWith: ComparedWith) :
        ProgressPresentationModel

    data class Same(override val comparedWith: ComparedWith) : ProgressPresentationModel

    sealed interface ComparedWith {
        data class WholeMonth(val month: YearMonth) : ComparedWith

        data object SameDaysLastMonth : ComparedWith
    }
}
