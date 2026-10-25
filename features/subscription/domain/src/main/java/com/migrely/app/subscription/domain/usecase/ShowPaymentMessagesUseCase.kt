package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.repository.GetAttackInProgressRepository
import com.migrely.app.subscription.domain.repository.ShowPaymentMessagesRepository

/**
 * Lets Google Play show its own messages about a payment that failed, in a grace period or on
 * account hold, as the app starts (BRD EC-BIL-03, EC-BIL-04). Never during an attack. When the user
 * fixes the subscription there, Google Play is asked again at once.
 */
class ShowPaymentMessagesUseCase(
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val showPaymentMessagesRepository: ShowPaymentMessagesRepository,
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) {
        if (getAttackInProgressRepository.attackInProgress() == false &&
            showPaymentMessagesRepository.showPaymentMessages()
        ) {
            premiumChecker.check()
        }
    }
}
