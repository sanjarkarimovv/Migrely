package com.migrely.app.settings.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.settings.presentation.model.DeleteAllDataPresentationNotification

class DeleteAllDataNotificationUiMapper(private val presenter: DeleteAllDataEventPresenter) :
    NotificationUiMapper<DeleteAllDataPresentationNotification> {
    override fun toUi(notification: DeleteAllDataPresentationNotification): UiNotification =
        when (notification) {
            DeleteAllDataPresentationNotification.NotDeleted ->
                UiNotification { presenter.showNotDeleted() }
        }
}
