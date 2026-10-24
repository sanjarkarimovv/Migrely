package com.migrely.app.history.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.history.presentation.model.MonthSkyPresentationNotification

/** The month card has no notifications (MonthSkyPresentationNotification has no members), so this is never asked. */
class MonthSkyNotificationUiMapper : NotificationUiMapper<MonthSkyPresentationNotification> {
    override fun toUi(notification: MonthSkyPresentationNotification): UiNotification =
        UiNotification { }
}
