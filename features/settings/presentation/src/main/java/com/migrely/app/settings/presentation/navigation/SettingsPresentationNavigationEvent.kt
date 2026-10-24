package com.migrely.app.settings.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface SettingsPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : SettingsPresentationNavigationEvent

    data object OnReminders : SettingsPresentationNavigationEvent

    data object OnYourData : SettingsPresentationNavigationEvent

    data object OnWarningSigns : SettingsPresentationNavigationEvent

    data object OnAboutMigrely : SettingsPresentationNavigationEvent

    data object OnPrivacy : SettingsPresentationNavigationEvent

    data object OnLicences : SettingsPresentationNavigationEvent

    /** Everything is deleted: the app starts again as on a new phone, with onboarding (BRD D96). */
    data object OnAllDataDeleted : SettingsPresentationNavigationEvent
}
