package com.migrely.app.home.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface HomePresentationNavigationEvent : PresentationNavigationEvent {
    data class OnViewAttackDetails(val attackId: String) : HomePresentationNavigationEvent

    /** The user wants to log an attack that has already ended (BRD ATK-F3, D26). */
    data object OnAddPastAttack : HomePresentationNavigationEvent

    /**
     * The user wants to log a medication taken (BRD D34): during the attack [attackId] from attack mode, or on its own
     * from the normal mode when null.
     */
    data class OnLogMedication(val attackId: String?) : HomePresentationNavigationEvent

    /** The user wants the app's settings, reminders among them (BRD D65, D70). */
    data object OnOpenSettings : HomePresentationNavigationEvent
}
