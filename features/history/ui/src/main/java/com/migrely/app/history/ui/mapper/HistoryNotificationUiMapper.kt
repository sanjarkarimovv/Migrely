package com.migrely.app.history.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.history.presentation.model.HistoryPresentationNotification
import com.migrely.app.history.ui.model.HistoryMessageUiModel
import com.migrely.app.history.ui.notification.HistoryMessagePresenter

class HistoryNotificationUiMapper(private val historyMessagePresenter: HistoryMessagePresenter) :
    NotificationUiMapper<HistoryPresentationNotification> {
    override fun toUi(notification: HistoryPresentationNotification): UiNotification {
        val message = when (notification) {
            is HistoryPresentationNotification.AttackDeleted -> HistoryMessageUiModel.AttackDeleted(
                notification.attackId,
                notification.deletedAt.toEpochMilli()
            )
            HistoryPresentationNotification.Undone -> HistoryMessageUiModel.Undone
            HistoryPresentationNotification.UndoNotPossible -> HistoryMessageUiModel.UndoNotPossible
            HistoryPresentationNotification.AttackLoggedOnDay ->
                HistoryMessageUiModel.AttackLoggedOnDay
            HistoryPresentationNotification.StorageFull -> HistoryMessageUiModel.StorageFull
            HistoryPresentationNotification.SomethingWentWrong ->
                HistoryMessageUiModel.SomethingWentWrong
        }
        return UiNotification { historyMessagePresenter.show(message) }
    }
}
