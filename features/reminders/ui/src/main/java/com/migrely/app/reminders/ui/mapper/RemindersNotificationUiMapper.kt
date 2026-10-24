package com.migrely.app.reminders.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.ui.model.ReminderKindUiModel
import com.migrely.app.reminders.ui.model.RemindersEventUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel

class RemindersNotificationUiMapper(private val presenter: RemindersEventPresenter) :
    NotificationUiMapper<RemindersPresentationNotification> {
    override fun toUi(notification: RemindersPresentationNotification): UiNotification {
        val event = when (notification) {
            is RemindersPresentationNotification.AskForPermission ->
                RemindersEventUiModel.AskForPermission(
                    when (notification.kind) {
                        ReminderKindPresentationModel.Daily -> ReminderKindUiModel.Daily
                        ReminderKindPresentationModel.Effect -> ReminderKindUiModel.Effect
                    }
                )
            RemindersPresentationNotification.StorageFull ->
                RemindersEventUiModel.Message(RemindersMessageUiModel.StorageFull)
            RemindersPresentationNotification.SomethingWentWrong ->
                RemindersEventUiModel.Message(RemindersMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { presenter.show(event) }
    }
}
