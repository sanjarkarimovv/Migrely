package com.migrely.app.onboarding.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.onboarding.presentation.model.OnboardingPresentationNotification

class OnboardingNotificationUiMapper(private val presenter: OnboardingEventPresenter) :
    NotificationUiMapper<OnboardingPresentationNotification> {
    override fun toUi(notification: OnboardingPresentationNotification): UiNotification =
        when (notification) {
            OnboardingPresentationNotification.NotSaved -> UiNotification {
                presenter.showNotSaved()
            }
        }
}
