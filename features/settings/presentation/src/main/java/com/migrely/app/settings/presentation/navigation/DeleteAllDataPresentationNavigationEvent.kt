package com.migrely.app.settings.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface DeleteAllDataPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : DeleteAllDataPresentationNavigationEvent

    /** Everything is deleted: the app starts again as on a new phone, with onboarding (BRD D96). */
    data object OnAllDataDeleted : DeleteAllDataPresentationNavigationEvent
}
