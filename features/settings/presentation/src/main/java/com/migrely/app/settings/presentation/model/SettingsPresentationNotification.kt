package com.migrely.app.settings.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface SettingsPresentationNotification : PresentationNotification {
    /** A setting could not be saved; the screen shows it as it is. */
    data object NotSaved : SettingsPresentationNotification
}
