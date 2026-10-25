package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.onboarding.presentation.navigation.OnboardingPresentationNavigationEvent
import com.migrely.app.ui.main.route.Onboarding
import com.migrely.app.ui.main.route.PaywallGate
import com.migrely.app.ui.main.route.YourData

class OnboardingNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<OnboardingPresentationNavigationEvent>(
        OnboardingPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: OnboardingPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        // The paywall gate takes onboarding's place, and then the paywall or home takes the gate's,
        // so Back from either leaves the app (change add-subscription, design D4).
        OnboardingPresentationNavigationEvent.OnFinished -> UiDestination {
            it.navigate(
                PaywallGate,
                navOptions {
                    popUpTo<Onboarding> { inclusive = true }
                    launchSingleTop = true
                }
            )
        }

        OnboardingPresentationNavigationEvent.OnRestoreBackup -> UiDestination {
            it.navigate(YourData, navOptions { launchSingleTop = true })
        }
    }
}
