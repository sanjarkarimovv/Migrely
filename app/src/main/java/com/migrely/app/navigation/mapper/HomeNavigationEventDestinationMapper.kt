package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
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
        }
}
