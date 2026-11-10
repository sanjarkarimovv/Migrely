package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.reminders.presentation.navigation.VisitCardPresentationNavigationEvent
import com.migrely.app.ui.main.route.History

/**
 * The visit card's "Make a report" opens History, which opens the report through its own premium gate, so the
 * report, or the paywall with "A report for your doctor", lies over History and Back returns to it (design D6).
 */
class VisitCardNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<VisitCardPresentationNavigationEvent>(
        VisitCardPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: VisitCardPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        VisitCardPresentationNavigationEvent.OnOpenReport -> UiDestination { navController ->
            navController.navigate(
                History(openReport = true),
                navOptions {
                    launchSingleTop =
                        true
                }
            )
        }
    }
}
