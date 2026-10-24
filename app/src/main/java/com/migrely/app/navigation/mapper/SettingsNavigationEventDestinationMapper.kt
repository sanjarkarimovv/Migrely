package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent
import com.migrely.app.ui.main.route.Reminders
import com.migrely.app.ui.main.route.Settings
import com.migrely.app.ui.main.route.YourData

class SettingsNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<SettingsPresentationNavigationEvent>(
        SettingsPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: SettingsPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        SettingsPresentationNavigationEvent.OnBack -> UiDestination {
            it.popBackStack(Settings::class, inclusive = true)
        }

        SettingsPresentationNavigationEvent.OnReminders -> UiDestination {
            it.navigate(Reminders, navOptions { launchSingleTop = true })
        }

        SettingsPresentationNavigationEvent.OnYourData -> UiDestination {
            it.navigate(YourData, navOptions { launchSingleTop = true })
        }
    }
}
