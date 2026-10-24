package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.History

/**
 * History opens an attack's details or the add screen on top of itself, so back returns to History (BRD D26, D30). A
 * double tap opens either once. Leaving pops History, also when a second tap arrives during the exit animation.
 */
class HistoryNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<HistoryPresentationNavigationEvent>(
        HistoryPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(navigationEvent: HistoryPresentationNavigationEvent): UiDestination =
        when (navigationEvent) {
            is HistoryPresentationNavigationEvent.OnViewAttackDetails -> {
                val route = AttackDetails(navigationEvent.attackId)
                UiDestination { it.navigate(route, navOptions { launchSingleTop = true }) }
            }

            is HistoryPresentationNavigationEvent.OnAddPastAttack -> {
                val route = AddPastAttack(navigationEvent.day?.toEpochDay())
                UiDestination { it.navigate(route, navOptions { launchSingleTop = true }) }
            }

            HistoryPresentationNavigationEvent.OnLeave -> UiDestination { navController ->
                navController.popBackStack(History::class, inclusive = true)
            }
        }
}
