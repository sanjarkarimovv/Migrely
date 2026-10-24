package com.migrely.app.report.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.report.domain.model.CreateReportRequestDomainModel
import com.migrely.app.report.domain.model.CreateReportResultDomainModel
import com.migrely.app.report.domain.usecase.CreateReportUseCase
import com.migrely.app.report.domain.usecase.GetReportOptionsUseCase
import com.migrely.app.report.presentation.mapper.ReportOptionsPresentationMapper
import com.migrely.app.report.presentation.mapper.ReportPeriodDomainMapper
import com.migrely.app.report.presentation.model.ReportPeriodPresentationModel
import com.migrely.app.report.presentation.model.ReportPresentationNotification
import com.migrely.app.report.presentation.model.ReportViewState
import com.migrely.app.report.presentation.navigation.ReportPresentationNavigationEvent

/**
 * The doctor report (BRD REP-F1–F4, D81–D85). The UI keeps the chosen period and the notes switch and passes them with
 * each event. Making a large report can outlast a rotation, so, as on Your data, this view model lives as long as the
 * app and keeps only for delivery whether a report is being made and an outcome the screen has not taken (ARCHITECTURE
 * section 13).
 */
class ReportViewModel(
    private val getReportOptionsUseCase: GetReportOptionsUseCase,
    private val createReportUseCase: CreateReportUseCase,
    private val reportPeriodDomainMapper: ReportPeriodDomainMapper,
    private val reportOptionsPresentationMapper: ReportOptionsPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<ReportViewState, ReportPresentationNotification>(useCaseExecutor) {
    private var working = false
    private var undelivered: ReportPresentationNotification? = null

    /** Only the latest read of the options is shown, so a quick change of period never shows stale dates. */
    private var latestLoad = 0

    /** A new visit: an outcome left from an earlier one is not shown again. */
    fun onEnter(period: ReportPeriodPresentationModel) {
        undelivered = null
        load(period)
    }

    /** The screen listens again, after a rotation for example: it gets the state and an outcome it missed. */
    fun onResume(period: ReportPeriodPresentationModel) {
        load(period)
        undelivered?.let(::notify)
    }

    fun onPeriodAction(period: ReportPeriodPresentationModel) = load(period)

    fun onOutcomeShownAction() {
        undelivered = null
    }

    fun onBackAction() = emitNavigationEvent(ReportPresentationNavigationEvent.OnBack)

    fun onCreateAction(period: ReportPeriodPresentationModel, includeNotes: Boolean) {
        // Two taps in one frame make one report: both would write the same file (review I4).
        if (working) return
        working = true
        updateViewState(ReportViewState.Working)
        createReportUseCase(
            value = CreateReportRequestDomainModel(
                reportPeriodDomainMapper.toDomain(period),
                includeNotes
            ),
            onResult = { result ->
                working = false
                val outcome = outcomeOf(result)
                undelivered = outcome
                notify(outcome)
                load(period)
            }
        )
    }

    private fun outcomeOf(result: CreateReportResultDomainModel) = when (result) {
        is CreateReportResultDomainModel.Created -> ReportPresentationNotification.Share(result.uri)
        CreateReportResultDomainModel.NothingLogged -> ReportPresentationNotification.NothingLogged
        CreateReportResultDomainModel.StorageFull -> ReportPresentationNotification.StorageFull
        CreateReportResultDomainModel.Failed -> ReportPresentationNotification.SomethingWentWrong
    }

    private fun load(period: ReportPeriodPresentationModel) {
        if (working) {
            updateViewState(ReportViewState.Working)
            return
        }
        getReportOptionsUseCase(
            value = reportPeriodDomainMapper.toDomain(period),
            onResult = {
                if (!working) updateViewState(reportOptionsPresentationMapper.toPresentation(it))
            }
        )
    }
}
