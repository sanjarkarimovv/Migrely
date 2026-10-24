package com.migrely.app.reminders.presentation.model

import java.time.LocalTime

sealed interface DailyReminderOfferViewState {
    data object Loading : DailyReminderOfferViewState

    /** The offer, at the reminder's saved time (8:00 PM unless the user chose another). */
    data class Ready(val time: LocalTime) : DailyReminderOfferViewState
}
