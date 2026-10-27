package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.usecase.ShouldShowPaywallUseCase
import com.migrely.app.subscription.presentation.model.PaywallTriggerPresentationNotification
import com.migrely.app.subscription.presentation.navigation.PaywallTriggerPresentationNavigationEvent

/**
 * Home's paywall after the 5th migraine (BRD §7.3, D110, design D5). It asks every time home loads
 * in today's mode, never in attack mode, and opens the paywall once when the rules allow. It draws
 * nothing, so its view state is [Unit].
 */
class PaywallTriggerViewModel(
    private val shouldShowPaywallUseCase: ShouldShowPaywallUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<Unit, PaywallTriggerPresentationNotification>(useCaseExecutor) {
    /** Home loads several times in a row; one question at a time opens one paywall. */
    private var asking = false

    fun onEnter() {
        if (asking) return
        asking = true
        shouldShowPaywallUseCase(
            value = PaywallTriggerDomainModel.FifthMigraine,
            onResult = { show ->
                asking = false
                if (show) {
                    emitNavigationEvent(PaywallTriggerPresentationNavigationEvent.OnOpenPaywall)
                }
            }
        )
    }
}
