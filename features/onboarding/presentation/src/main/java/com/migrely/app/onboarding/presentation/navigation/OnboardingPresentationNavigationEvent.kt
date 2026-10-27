package com.migrely.app.onboarding.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface OnboardingPresentationNavigationEvent : PresentationNavigationEvent {
    /** Onboarding is done; home takes its place. */
    data object OnFinished : OnboardingPresentationNavigationEvent

    /** The user wants their diary back from a backup file (BRD EC-BAK-05). */
    data object OnRestoreBackup : OnboardingPresentationNavigationEvent
}
