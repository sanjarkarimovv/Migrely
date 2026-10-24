package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.DailyReminderOfferPresentationNotification
import com.migrely.app.reminders.ui.model.DailyReminderOfferEventUiModel

/** The offer is not a screen, so it reads its view model's notifications itself, through this mapper. */
class DailyReminderOfferEventUiMapper {
    fun toUi(notification: DailyReminderOfferPresentationNotification) = when (notification) {
        DailyReminderOfferPresentationNotification.AskForPermission ->
            DailyReminderOfferEventUiModel.AskForPermission
        DailyReminderOfferPresentationNotification.Done -> DailyReminderOfferEventUiModel.Done
    }
}
