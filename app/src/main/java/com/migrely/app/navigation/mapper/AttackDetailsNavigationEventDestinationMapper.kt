package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.LogMedication

/**
 * Both ways out of the details screen return to where it was opened from; home then offers any undo (BRD D23). Popping
 * up to the details route, not one entry, makes a second tap during the exit animation do nothing instead of leaving
 * an empty screen.
 */
class AttackDetailsNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<AttackDetailsPresentationNavigationEvent>(
        AttackDetailsPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: AttackDetailsPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        AttackDetailsPresentationNavigationEvent.OnLeave,
        AttackDetailsPresentationNavigationEvent.OnAttackDeleted -> UiDestination { navController ->
            navController.popBackStack(AttackDetails::class, inclusive = true)
        }

        is AttackDetailsPresentationNavigationEvent.OnLogMedication -> {
            val route = LogMedication(navigationEvent.attackId)
            UiDestination { it.navigate(route, navOptions { launchSingleTop = true }) }
        }
    }
}
