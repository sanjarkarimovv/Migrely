package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.settings.presentation.model.SettingsPagePresentationModel
import com.migrely.app.settings.presentation.navigation.SettingsPagePresentationNavigationEvent
import com.migrely.app.ui.main.route.AboutMigrely
import com.migrely.app.ui.main.route.Licences
import com.migrely.app.ui.main.route.Privacy
import com.migrely.app.ui.main.route.WarningSigns

/** Back leaves the page it came from, so a double tap never leaves Settings too. */
class SettingsPageNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<SettingsPagePresentationNavigationEvent>(
        SettingsPagePresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: SettingsPagePresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        is SettingsPagePresentationNavigationEvent.OnBack -> {
            val route = when (navigationEvent.page) {
                SettingsPagePresentationModel.WarningSigns -> WarningSigns::class
                SettingsPagePresentationModel.AboutMigrely -> AboutMigrely::class
                SettingsPagePresentationModel.Privacy -> Privacy::class
                SettingsPagePresentationModel.Licences -> Licences::class
            }
            UiDestination { it.popBackStack(route, inclusive = true) }
        }
    }
}
