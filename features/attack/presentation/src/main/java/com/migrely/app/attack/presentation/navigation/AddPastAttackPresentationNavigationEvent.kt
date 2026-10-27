package com.migrely.app.attack.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface AddPastAttackPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnLeave : AddPastAttackPresentationNavigationEvent

    /** The new attack is stored; its details come next (BRD D26). */
    data class OnPastAttackAdded(val attackId: String) : AddPastAttackPresentationNavigationEvent

    /** The new attack was merged into [attackId] (BRD D27). */
    data class OnAttacksMerged(val attackId: String) : AddPastAttackPresentationNavigationEvent
}
