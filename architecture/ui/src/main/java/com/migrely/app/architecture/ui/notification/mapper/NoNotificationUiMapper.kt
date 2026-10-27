package com.migrely.app.architecture.ui.notification.mapper

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.ui.notification.model.UiNotification

/**
 * For screens whose ViewModel never emits notifications.
 */
class NoNotificationUiMapper : NotificationUiMapper<PresentationNotification> {
    override fun toUi(notification: PresentationNotification): UiNotification = UiNotification { }
}
