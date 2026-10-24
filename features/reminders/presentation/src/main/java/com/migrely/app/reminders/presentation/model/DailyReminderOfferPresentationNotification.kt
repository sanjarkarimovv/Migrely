package com.migrely.app.reminders.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface DailyReminderOfferPresentationNotification : PresentationNotification {
    /** Ask the system for notification permission (BRD EC-REM-01). */
    data object AskForPermission : DailyReminderOfferPresentationNotification

    /** The offer is answered, whatever the answer; whoever shows it moves on (BRD D80). */
    data object Done : DailyReminderOfferPresentationNotification
}
