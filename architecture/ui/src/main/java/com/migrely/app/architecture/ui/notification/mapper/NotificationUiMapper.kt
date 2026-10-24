package com.migrely.app.architecture.ui.notification.mapper

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.ui.notification.model.UiNotification

interface NotificationUiMapper<in PRESENTATION_NOTIFICATION : PresentationNotification> {
    fun toUi(notification: PRESENTATION_NOTIFICATION): UiNotification
}
