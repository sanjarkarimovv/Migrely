package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.repository.MarkSafetyCardShownRepository

/** The medication overuse card (BRD MED-F5, EC-MED-06) appeared: no rating is asked for in the next 3 days (BRD D188, EC-REV-14). */
class MarkSafetyCardShownUseCase(
    private val markSafetyCardShownRepository: MarkSafetyCardShownRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) =
        markSafetyCardShownRepository.markSafetyCardShown()
}
