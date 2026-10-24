package com.migrely.app.home.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface HomePresentationNavigationEvent : PresentationNavigationEvent {
    data class OnViewAttackDetails(val attackId: String) : HomePresentationNavigationEvent

    /** The user wants to log an attack that has already ended (BRD ATK-F3, D26). */
    data object OnAddPastAttack : HomePresentationNavigationEvent
}
