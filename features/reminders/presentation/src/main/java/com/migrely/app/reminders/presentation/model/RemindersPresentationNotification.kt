package com.migrely.app.reminders.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface RemindersPresentationNotification : PresentationNotification {
    /** Ask the system for notification permission, for [kind] (BRD EC-REM-01). */
    data class AskForPermission(val kind: ReminderKindPresentationModel) :
        RemindersPresentationNotification

    data object StorageFull : RemindersPresentationNotification

    data object SomethingWentWrong : RemindersPresentationNotification
}
