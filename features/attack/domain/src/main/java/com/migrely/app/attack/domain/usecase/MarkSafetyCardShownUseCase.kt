package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.attack.domain.repository.MarkSafetyCardShownRepository

/** The first-10 safety card (BRD SAF-F2) appeared: no rating is asked for in the next 3 days (BRD D188, EC-REV-14). */
class MarkSafetyCardShownUseCase(
    private val markSafetyCardShownRepository: MarkSafetyCardShownRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) =
        markSafetyCardShownRepository.markSafetyCardShown()
}
