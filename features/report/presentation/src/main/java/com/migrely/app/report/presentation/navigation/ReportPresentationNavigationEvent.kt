package com.migrely.app.report.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface ReportPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : ReportPresentationNavigationEvent
}
