package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.repository.MarkSafetyCardShownRepository

/** "Still going?" (BRD SAF-F3, EC-ATK-02) appeared: no rating is asked for in the next 3 days (BRD D188, EC-REV-14). */
class MarkSafetyCardShownUseCase(
    private val markSafetyCardShownRepository: MarkSafetyCardShownRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) =
        markSafetyCardShownRepository.markSafetyCardShown()
}
