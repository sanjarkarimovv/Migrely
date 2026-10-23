package com.migrely.app.home.ui.di

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.home.presentation.viewmodel.HomeViewModel
import com.migrely.app.home.ui.mapper.HomeViewStateUiMapper

class HomeDependencies(
    val homeViewModel: HomeViewModel,
    val homeViewStateUiMapper: HomeViewStateUiMapper,
    homeNavigationMapper: NavigationEventDestinationMapper<HomePresentationNavigationEvent>,
    homeNotificationMapper: NotificationUiMapper<PresentationNotification>
) : BaseComposeHolder<HomeViewState, PresentationNotification>(
    homeViewModel,
    homeNavigationMapper,
    homeNotificationMapper
)
