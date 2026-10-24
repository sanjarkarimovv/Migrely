package com.migrely.app.settings.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification

/** Settings sends no notifications yet, so there is nothing to show. */
class SettingsNotificationUiMapper : NotificationUiMapper<SettingsPresentationNotification> {
    override fun toUi(notification: SettingsPresentationNotification) = UiNotification { }
}
