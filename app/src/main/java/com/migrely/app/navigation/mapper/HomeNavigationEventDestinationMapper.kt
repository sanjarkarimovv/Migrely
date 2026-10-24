package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.LogMedication
import com.migrely.app.ui.main.route.Reminders

class HomeNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<HomePresentationNavigationEvent>(
        HomePresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(navigationEvent: HomePresentationNavigationEvent): UiDestination =
        when (navigationEvent) {
            is HomePresentationNavigationEvent.OnViewAttackDetails -> {
                val route = AttackDetails(navigationEvent.attackId)
                UiDestination { it.navigate(route) }
            }

            // A double tap on the link opens the add screen once.
            HomePresentationNavigationEvent.OnAddPastAttack -> UiDestination { navController ->
                navController.navigate(AddPastAttack(), navOptions { launchSingleTop = true })
            }

            // From attack mode the screen keeps the dark room (BRD D34, 18.3 B).
            is HomePresentationNavigationEvent.OnLogMedication -> {
                val route = LogMedication(
                    navigationEvent.attackId,
                    darkRoom = navigationEvent.attackId != null
                )
                UiDestination { it.navigate(route, navOptions { launchSingleTop = true }) }
            }

            HomePresentationNavigationEvent.OnOpenReminders -> UiDestination { navController ->
                navController.navigate(Reminders, navOptions { launchSingleTop = true })
            }
        }
}
