package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.usecase.ShouldShowPaywallUseCase
import com.migrely.app.subscription.presentation.model.PaywallTriggerPresentationNotification
import com.migrely.app.subscription.presentation.navigation.PaywallGatePresentationNavigationEvent

/**
 * Between onboarding and home (design D4): the paywall shows itself once for a free user, or home
 * follows at once. It draws nothing but the background, so its view state is [Unit].
 */
class PaywallGateViewModel(
    private val shouldShowPaywallUseCase: ShouldShowPaywallUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<Unit, PaywallTriggerPresentationNotification>(useCaseExecutor) {
    fun onEnter() {
        shouldShowPaywallUseCase(
            value = PaywallTriggerDomainModel.Onboarding,
            onResult = { show ->
                emitNavigationEvent(
                    if (show) {
                        PaywallGatePresentationNavigationEvent.OnShowPaywall
                    } else {
                        PaywallGatePresentationNavigationEvent.OnContinue
                    }
                )
            }
        )
    }
}
