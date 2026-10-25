package com.migrely.app.subscription.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.subscription.presentation.model.PaywallTriggerPresentationNotification

/** The home trigger and the onboarding gate have no notifications, so this is never asked. */
class PaywallTriggerNotificationUiMapper :
    NotificationUiMapper<PaywallTriggerPresentationNotification> {
    override fun toUi(notification: PaywallTriggerPresentationNotification): UiNotification =
        UiNotification { }
}
