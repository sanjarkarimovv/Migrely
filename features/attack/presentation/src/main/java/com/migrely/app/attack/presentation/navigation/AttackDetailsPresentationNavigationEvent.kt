package com.migrely.app.attack.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface AttackDetailsPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnLeave : AttackDetailsPresentationNavigationEvent

    /** The attack was deleted; home offers the undo (BRD D23). */
    data object OnAttackDeleted : AttackDetailsPresentationNavigationEvent
}
