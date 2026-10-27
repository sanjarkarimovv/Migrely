package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent
import com.migrely.app.ui.main.route.AboutMigrely
import com.migrely.app.ui.main.route.DeleteAllData
import com.migrely.app.ui.main.route.Licences
import com.migrely.app.ui.main.route.Privacy
import com.migrely.app.ui.main.route.Reminders
import com.migrely.app.ui.main.route.Settings
import com.migrely.app.ui.main.route.WarningSigns
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

        SettingsPresentationNavigationEvent.OnReminders -> open(Reminders)
        SettingsPresentationNavigationEvent.OnYourData -> open(YourData)
        SettingsPresentationNavigationEvent.OnWarningSigns -> open(WarningSigns)
        SettingsPresentationNavigationEvent.OnAboutMigrely -> open(AboutMigrely)
        SettingsPresentationNavigationEvent.OnPrivacy -> open(Privacy)
        SettingsPresentationNavigationEvent.OnLicences -> open(Licences)
        SettingsPresentationNavigationEvent.OnDeleteAllData -> open(DeleteAllData)
    }

    private fun open(route: Any) = UiDestination {
        it.navigate(route, navOptions { launchSingleTop = true })
    }
}
