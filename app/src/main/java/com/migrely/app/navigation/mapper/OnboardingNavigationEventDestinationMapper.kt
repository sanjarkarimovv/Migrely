package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.onboarding.presentation.navigation.OnboardingPresentationNavigationEvent
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Onboarding
import com.migrely.app.ui.main.route.YourData

class OnboardingNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<OnboardingPresentationNavigationEvent>(
        OnboardingPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: OnboardingPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        // Home takes onboarding's place, so Back from home leaves the app.
        OnboardingPresentationNavigationEvent.OnFinished -> UiDestination {
            it.navigate(
                Home(),
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
