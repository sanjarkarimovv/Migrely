package com.migrely.app.report.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.model.ReportCreditWriteResultDomainModel
import com.migrely.app.report.domain.repository.SetReportCreditRepository

/** Keeps the credit switch; the next report follows it, a report already shared does not change (BRD D186, EC-REP-08). */
class SetReportCreditUseCase(
    private val setReportCreditRepository: SetReportCreditRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Boolean, ReportCreditWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Boolean): ReportCreditWriteResultDomainModel =
        setReportCreditRepository.setReportCreditShown(request)
}
