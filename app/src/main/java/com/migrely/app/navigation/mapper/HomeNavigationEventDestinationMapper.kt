package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails

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
        }
}
