package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.history.presentation.model.PremiumFeaturePresentationModel
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.History
import com.migrely.app.ui.main.route.Insights
import com.migrely.app.ui.main.route.PaywallOrigin
import com.migrely.app.ui.main.route.Report

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

            HistoryPresentationNavigationEvent.OnOpenReport -> UiDestination { navController ->
                navController.navigate(Report, navOptions { launchSingleTop = true })
            }

            HistoryPresentationNavigationEvent.OnOpenInsights -> UiDestination { navController ->
                navController.navigate(Insights, navOptions { launchSingleTop = true })
            }

            // A free user's tap opens the paywall with that feature's headline (BRD D108).
            is HistoryPresentationNavigationEvent.OnOpenPaywall -> openPaywall(
                when (navigationEvent.feature) {
                    PremiumFeaturePresentationModel.Report -> PaywallOrigin.Report
                    PremiumFeaturePresentationModel.Insights -> PaywallOrigin.Insights
                }
            )
        }
}
