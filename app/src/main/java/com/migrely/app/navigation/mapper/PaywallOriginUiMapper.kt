package com.migrely.app.navigation.mapper

import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.ui.main.route.PaywallOrigin

/** A paywall route's origin as the paywall screen takes it; an unknown one reads as Settings. */
class PaywallOriginUiMapper {
    fun toUi(origin: String): PaywallContextUiModel =
        when (PaywallOrigin.entries.firstOrNull { it.name == origin }) {
            PaywallOrigin.Onboarding -> PaywallContextUiModel.Onboarding
            PaywallOrigin.Patterns -> PaywallContextUiModel.Patterns
            PaywallOrigin.Report -> PaywallContextUiModel.Report
            PaywallOrigin.Insights -> PaywallContextUiModel.Insights
            PaywallOrigin.Settings, null -> PaywallContextUiModel.Settings
        }
}
