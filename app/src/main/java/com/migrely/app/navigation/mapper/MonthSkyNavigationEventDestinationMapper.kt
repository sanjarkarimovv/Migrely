package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.history.presentation.navigation.MonthSkyPresentationNavigationEvent
import com.migrely.app.ui.main.route.History

/** Home's month card opens History on the month or on a day; a double tap opens it once (BRD D29). */
class MonthSkyNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<MonthSkyPresentationNavigationEvent>(
        MonthSkyPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: MonthSkyPresentationNavigationEvent
    ): UiDestination {
        val route = when (navigationEvent) {
            MonthSkyPresentationNavigationEvent.OnViewMonth -> History()
            is MonthSkyPresentationNavigationEvent.OnViewDay ->
                History(navigationEvent.day.toEpochDay())
        }
        return UiDestination { it.navigate(route, navOptions { launchSingleTop = true }) }
    }
}
