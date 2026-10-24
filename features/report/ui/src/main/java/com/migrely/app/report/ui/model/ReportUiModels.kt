package com.migrely.app.report.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The chosen period, kept across a rotation and process death. */
sealed interface ReportPeriodUiModel : Parcelable {
    @Parcelize
    data class LastMonths(val months: Int) : ReportPeriodUiModel

    @Parcelize
    data class Range(val fromEpochDay: Long, val toEpochDay: Long) : ReportPeriodUiModel
}

@Parcelize
data class ReportOptionsUiModel(
    val fromEpochDay: Long,
    val toEpochDay: Long,
    val anythingLogged: Boolean
) : Parcelable

sealed interface ReportViewStateUiModel {
    data object Loading : ReportViewStateUiModel

    data class Ready(val options: ReportOptionsUiModel) : ReportViewStateUiModel

    data object Working : ReportViewStateUiModel

    data object Unavailable : ReportViewStateUiModel
}

sealed interface ReportEventUiModel {
    data class Share(val uri: String) : ReportEventUiModel

    data class Message(val message: ReportMessageUiModel) : ReportEventUiModel
}

enum class ReportMessageUiModel {
    NothingLogged,
    StorageFull,
    SomethingWentWrong,
    NoAppToShare
}
