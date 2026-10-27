package com.migrely.app.report.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.repository.CleanUpReportsRepository

/** Run when the app starts, so a report cut off by the app closing leaves nothing behind (BRD EC-REP-03). */
class CleanUpReportsUseCase(
    private val cleanUpReportsRepository: CleanUpReportsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) = cleanUpReportsRepository.cleanUpReports()
}
