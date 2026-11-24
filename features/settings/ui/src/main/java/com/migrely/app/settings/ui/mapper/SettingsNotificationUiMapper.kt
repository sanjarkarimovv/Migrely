package com.migrely.app.settings.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.ui.model.SettingsEventUiModel

class SettingsNotificationUiMapper(private val presenter: SettingsEventPresenter) :
    NotificationUiMapper<SettingsPresentationNotification> {
    override fun toUi(notification: SettingsPresentationNotification): UiNotification {
        val event = when (notification) {
            SettingsPresentationNotification.NotSaved -> SettingsEventUiModel.NotSaved
            SettingsPresentationNotification.OpenShareSheet -> SettingsEventUiModel.OpenShareSheet
            SettingsPresentationNotification.OpenStorePage -> SettingsEventUiModel.OpenStorePage
        }
        return UiNotification { presenter.show(event) }
    }
}
