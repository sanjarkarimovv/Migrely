package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.navigation.PaywallPresentationNavigationEvent
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Insights
import com.migrely.app.ui.main.route.Paywall
import com.migrely.app.ui.main.route.Report

/**
 * The paywall (change add-subscription, design D4). Closing it goes back, or home after
 * onboarding. Once Premium is on, the feature it was opened from takes its place: the doctor
 * report, Insights for both Insights and the 5th-migraine patterns, back to Settings, or home after
 * onboarding. A second tap during the exit animation does nothing more.
 */
class PaywallNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<PaywallPresentationNavigationEvent>(
        PaywallPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(navigationEvent: PaywallPresentationNavigationEvent): UiDestination =
        when (navigationEvent) {
            is PaywallPresentationNavigationEvent.OnClose -> when (navigationEvent.context) {
                PaywallContextPresentationModel.Onboarding -> replaceWith(Home())
                PaywallContextPresentationModel.Patterns,
                PaywallContextPresentationModel.Report,
                PaywallContextPresentationModel.Insights,
                PaywallContextPresentationModel.Settings -> UiDestination(::leave)
            }

            is PaywallPresentationNavigationEvent.OnPremiumUnlocked ->
                when (navigationEvent.context) {
                    PaywallContextPresentationModel.Onboarding -> replaceWith(Home())
                    PaywallContextPresentationModel.Report -> replaceWith(Report)
                    PaywallContextPresentationModel.Patterns,
                    PaywallContextPresentationModel.Insights -> replaceWith(Insights)
                    PaywallContextPresentationModel.Settings -> UiDestination(::leave)
                }
        }

    private fun replaceWith(route: Any) = UiDestination { navController ->
        navController.navigate(
            route,
            navOptions {
                popUpTo<Paywall> { inclusive = true }
                launchSingleTop = true
            }
        )
    }

    private fun leave(navController: NavController) {
        navController.popBackStack(Paywall::class, inclusive = true)
    }
}
