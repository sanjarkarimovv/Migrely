package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.report.presentation.navigation.ReportPresentationNavigationEvent
import com.migrely.app.ui.main.route.Report

class ReportNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<ReportPresentationNavigationEvent>(
        ReportPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(navigationEvent: ReportPresentationNavigationEvent): UiDestination =
        when (navigationEvent) {
            ReportPresentationNavigationEvent.OnBack -> UiDestination {
                it.popBackStack(Report::class, inclusive = true)
            }
        }
}
