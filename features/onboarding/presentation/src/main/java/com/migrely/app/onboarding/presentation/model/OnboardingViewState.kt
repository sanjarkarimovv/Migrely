package com.migrely.app.onboarding.presentation.model

/** The steps are fixed and the UI keeps which one shows, so the only state is being shown. */
sealed interface OnboardingViewState {
    data object Ready : OnboardingViewState
}
