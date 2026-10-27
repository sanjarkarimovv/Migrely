package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.subscription.presentation.navigation.PaywallGatePresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PaywallTriggerPresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PremiumSectionPresentationNavigationEvent
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Paywall
import com.migrely.app.ui.main.route.PaywallGate
import com.migrely.app.ui.main.route.PaywallOrigin

/** Settings' Premium section opens the paywall on top of Settings, once (BRD D112). */
class PremiumSectionNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<PremiumSectionPresentationNavigationEvent>(
        PremiumSectionPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: PremiumSectionPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        PremiumSectionPresentationNavigationEvent.OnOpenPaywall ->
            openPaywall(PaywallOrigin.Settings)
    }
}

/** Home's 5th-migraine trigger opens the patterns paywall on top of home, once (BRD D110). */
class PaywallTriggerNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<PaywallTriggerPresentationNavigationEvent>(
        PaywallTriggerPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: PaywallTriggerPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        PaywallTriggerPresentationNavigationEvent.OnOpenPaywall ->
            openPaywall(PaywallOrigin.Patterns)
    }
}

/** After onboarding the paywall or home takes the gate's place, so Back leaves the app (D4). */
class PaywallGateNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<PaywallGatePresentationNavigationEvent>(
        PaywallGatePresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: PaywallGatePresentationNavigationEvent
    ): UiDestination {
        val route: Any = when (navigationEvent) {
            PaywallGatePresentationNavigationEvent.OnShowPaywall ->
                Paywall(PaywallOrigin.Onboarding.name)
            PaywallGatePresentationNavigationEvent.OnContinue -> Home()
        }
        return UiDestination { navController ->
            navController.navigate(
                route,
                navOptions {
                    popUpTo<PaywallGate> { inclusive = true }
                    launchSingleTop = true
                }
            )
        }
    }
}

internal fun openPaywall(origin: PaywallOrigin) = UiDestination { navController ->
    navController.navigate(Paywall(origin.name), navOptions { launchSingleTop = true })
}
