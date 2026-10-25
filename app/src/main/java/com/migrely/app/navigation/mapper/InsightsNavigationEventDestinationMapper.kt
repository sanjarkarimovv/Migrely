package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.insights.presentation.navigation.InsightsPresentationNavigationEvent
import com.migrely.app.ui.main.route.Insights

class InsightsNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<InsightsPresentationNavigationEvent>(
        InsightsPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: InsightsPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        InsightsPresentationNavigationEvent.OnBack -> UiDestination {
            it.popBackStack(Insights::class, inclusive = true)
        }
    }
}
