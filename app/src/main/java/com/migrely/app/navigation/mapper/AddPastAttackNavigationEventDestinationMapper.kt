package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.attack.presentation.navigation.AddPastAttackPresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails

/**
 * Leaving the add screen pops it. A new or merged attack replaces it with its details, so back from there returns home
 * (BRD D26); a late second event updates that details screen instead of stacking another one.
 */
class AddPastAttackNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<AddPastAttackPresentationNavigationEvent>(
        AddPastAttackPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: AddPastAttackPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        AddPastAttackPresentationNavigationEvent.OnLeave -> UiDestination { navController ->
            navController.popBackStack(AddPastAttack::class, inclusive = true)
        }

        is AddPastAttackPresentationNavigationEvent.OnPastAttackAdded ->
            details(AttackDetails(navigationEvent.attackId))

        is AddPastAttackPresentationNavigationEvent.OnAttacksMerged ->
            details(AttackDetails(navigationEvent.attackId, announceMerge = true))
    }

    private fun details(route: AttackDetails) = UiDestination { navController ->
        navController.navigate(
            route,
            navOptions {
                popUpTo<AddPastAttack> { inclusive = true }
                launchSingleTop = true
            }
        )
    }
}
