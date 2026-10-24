package com.migrely.app.reminders.ui.model

/** The offer, at [minuteOfDay] after midnight. */
data class DailyReminderOfferUiModel(val minuteOfDay: Int)

/** What the offer does in reply to its view model. */
enum class DailyReminderOfferEventUiModel {
    AskForPermission,
    Done
}
