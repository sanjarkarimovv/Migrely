package com.migrely.app.settings.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification

class SettingsNotificationUiMapper(private val presenter: SettingsEventPresenter) :
    NotificationUiMapper<SettingsPresentationNotification> {
    override fun toUi(notification: SettingsPresentationNotification): UiNotification =
        when (notification) {
            SettingsPresentationNotification.NotSaved -> UiNotification { presenter.showNotSaved() }
            SettingsPresentationNotification.NotDeleted ->
                UiNotification { presenter.showNotDeleted() }
        }
}
