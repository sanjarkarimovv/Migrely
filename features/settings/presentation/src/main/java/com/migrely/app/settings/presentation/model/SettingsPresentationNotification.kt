package com.migrely.app.settings.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface SettingsPresentationNotification : PresentationNotification {
    /** A setting could not be saved; the screen shows it as it is. */
    data object NotSaved : SettingsPresentationNotification

    /** "Share Migrely": the system share sheet with the link (BRD SET-F2, D185). */
    data object OpenShareSheet : SettingsPresentationNotification

    /** "Rate Migrely on Google Play": Migrely's store page, never the review flow (BRD SET-F2, D185). */
    data object OpenStorePage : SettingsPresentationNotification
}
