package com.migrely.app.onboarding.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.onboarding.presentation.model.OnboardingPresentationNotification
import com.migrely.app.onboarding.presentation.model.OnboardingViewState
import com.migrely.app.onboarding.presentation.navigation.OnboardingPresentationNavigationEvent
import com.migrely.app.onboarding.presentation.viewmodel.OnboardingViewModel
import com.migrely.app.onboarding.ui.mapper.OnboardingEventPresenter

class OnboardingDependencies(
    val onboardingViewModel: OnboardingViewModel,
    val onboardingEventPresenter: OnboardingEventPresenter,
    navigationMapper: NavigationEventDestinationMapper<OnboardingPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<OnboardingPresentationNotification>
) : BaseComposeHolder<OnboardingViewState, OnboardingPresentationNotification>(
    onboardingViewModel,
    navigationMapper,
    notificationMapper
)
