package com.migrely.app.report.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The chosen period, kept across a rotation and process death. */
sealed interface ReportPeriodUiModel : Parcelable {
    @Parcelize
    data class LastMonths(val months: Int) : ReportPeriodUiModel

    @Parcelize
    data class Range(val fromEpochDay: Long, val toEpochDay: Long) : ReportPeriodUiModel

    /** From the last doctor visit's day to today (BRD EC-REP-04). */
    @Parcelize
    data class SinceLastVisit(val fromEpochDay: Long) : ReportPeriodUiModel

    /** Nothing chosen yet: the options say which period that is. */
    @Parcelize
    data object Default : ReportPeriodUiModel
}

/**
 * [period] is the one the dates are for; [lastVisitEpochDay] is set when "Since last visit" is offered; [creditShown] is
 * the stored credit switch (BRD REP-F5, D186).
 */
@Parcelize
data class ReportOptionsUiModel(
    val fromEpochDay: Long,
    val toEpochDay: Long,
    val anythingLogged: Boolean,
    val period: ReportPeriodUiModel = ReportPeriodUiModel.Default,
    val lastVisitEpochDay: Long? = null,
    val creditShown: Boolean = true
) : Parcelable

sealed interface ReportViewStateUiModel {
    data object Loading : ReportViewStateUiModel

    data class Ready(val options: ReportOptionsUiModel) : ReportViewStateUiModel

    data object Working : ReportViewStateUiModel

    /** [creditShown] is the stored credit switch, which still shows (BRD EC-REP-12). */
    data class Unavailable(val creditShown: Boolean) : ReportViewStateUiModel
}

sealed interface ReportEventUiModel {
    data class Share(val uri: String) : ReportEventUiModel

    data class Message(val message: ReportMessageUiModel) : ReportEventUiModel
}

enum class ReportMessageUiModel {
    NothingLogged,
    StorageFull,
    SomethingWentWrong,
    NoAppToShare,
    NotSaved
}
