package com.migrely.app.backup.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface YourDataPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : YourDataPresentationNavigationEvent
}
