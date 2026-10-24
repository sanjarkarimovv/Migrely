package com.migrely.app.settings.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface SettingsPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : SettingsPresentationNavigationEvent

    data object OnReminders : SettingsPresentationNavigationEvent

    data object OnYourData : SettingsPresentationNavigationEvent
}
