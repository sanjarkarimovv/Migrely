package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.repository.DismissTrialBannerRepository
import java.time.Instant

/** Dismisses the trial reminder for the trial ending at the given time, so it never comes back. */
class DismissTrialReminderUseCase(
    private val dismissTrialBannerRepository: DismissTrialBannerRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Instant, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Instant) {
        dismissTrialBannerRepository.dismissTrialBanner(request)
    }
}
