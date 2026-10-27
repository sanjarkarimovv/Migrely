package com.migrely.app.insights.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface InsightsPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : InsightsPresentationNavigationEvent
}
