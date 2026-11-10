package com.migrely.app.report.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.LocalDate

sealed interface ReportPeriodPresentationModel {
    data class LastMonths(val months: Int) : ReportPeriodPresentationModel

    data class Range(val from: LocalDate, val to: LocalDate) : ReportPeriodPresentationModel

    data class SinceLastVisit(val from: LocalDate) : ReportPeriodPresentationModel

    /** Nothing chosen yet; the options say which period that is (BRD EC-REP-04). */
    data object Default : ReportPeriodPresentationModel
}

/**
 * [anythingLogged] is false when "Create report" must be off (BRD EC-REP-01). [period] is the one the dates are for,
 * never [ReportPeriodPresentationModel.Default], and [lastVisit] is set when "Since last visit" is offered (EC-REP-04).
 */
data class ReportOptionsPresentationModel(
    val from: LocalDate,
    val to: LocalDate,
    val anythingLogged: Boolean,
    val period: ReportPeriodPresentationModel,
    val lastVisit: LocalDate?
)

sealed interface ReportViewState {
    data object Loading : ReportViewState

    data class Ready(val options: ReportOptionsPresentationModel) : ReportViewState

    /** The report is being made (BRD EC-REP-02). */
    data object Working : ReportViewState

    data object Unavailable : ReportViewState
}

sealed interface ReportPresentationNotification : PresentationNotification {
    /** The report is ready to share; [uri] is its content URI. */
    data class Share(val uri: String) : ReportPresentationNotification

    data object NothingLogged : ReportPresentationNotification

    data object StorageFull : ReportPresentationNotification

    data object SomethingWentWrong : ReportPresentationNotification
}
