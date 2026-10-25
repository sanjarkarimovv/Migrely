package com.migrely.app.subscription.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel

sealed interface PaywallPresentationNavigationEvent : PresentationNavigationEvent {
    /** The paywall closes without a purchase: back, or home after onboarding (BRD D110). */
    data class OnClose(val context: PaywallContextPresentationModel) :
        PaywallPresentationNavigationEvent

    /** Premium is on: on to the feature the paywall was opened from (BRD D109). */
    data class OnPremiumUnlocked(val context: PaywallContextPresentationModel) :
        PaywallPresentationNavigationEvent
}

sealed interface PremiumSectionPresentationNavigationEvent : PresentationNavigationEvent {
    /** A free user, or a subscriber who may change plans, opens the paywall (BRD D112). */
    data object OnOpenPaywall : PremiumSectionPresentationNavigationEvent
}

sealed interface PaywallTriggerPresentationNavigationEvent : PresentationNavigationEvent {
    /** The 5th migraine is logged: the paywall shows itself on home (BRD D110). */
    data object OnOpenPaywall : PaywallTriggerPresentationNavigationEvent
}

sealed interface PaywallGatePresentationNavigationEvent : PresentationNavigationEvent {
    /** Onboarding is done and the paywall shows itself once (BRD D110, design D4). */
    data object OnShowPaywall : PaywallGatePresentationNavigationEvent

    /** Onboarding is done and home follows. */
    data object OnContinue : PaywallGatePresentationNavigationEvent
}
