package com.migrely.app.home.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface HomePresentationNavigationEvent : PresentationNavigationEvent {
    data class OnViewAttackDetails(val attackId: String) : HomePresentationNavigationEvent
}
