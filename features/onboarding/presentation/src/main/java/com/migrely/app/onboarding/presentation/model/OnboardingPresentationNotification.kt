package com.migrely.app.onboarding.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface OnboardingPresentationNotification : PresentationNotification {
    /** An answer could not be saved. Onboarding goes on; it can be set later in Settings. */
    data object NotSaved : OnboardingPresentationNotification
}
