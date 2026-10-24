package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** How a month's migraine days compare with the month before; [days] is 0 for [ProgressTrendUiModel.Same]. */
@Parcelize
data class ProgressUiModel(
    val trend: ProgressTrendUiModel,
    val days: Int,
    val against: ProgressAgainstUiModel
) : Parcelable

enum class ProgressTrendUiModel {
    Fewer,
    More,
    Same
}

sealed interface ProgressAgainstUiModel : Parcelable {
    /** The whole of a month; [month] is 1 to 12. */
    @Parcelize
    data class WholeMonth(val year: Int, val month: Int) : ProgressAgainstUiModel

    @Parcelize
    data object SameDaysLastMonth : ProgressAgainstUiModel
}
