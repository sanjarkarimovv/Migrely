package com.migrely.app.home.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.home.presentation.model.HomePresentationNotification
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.home.presentation.viewmodel.HomeViewModel
import com.migrely.app.home.ui.mapper.HomeViewStateUiMapper
import com.migrely.app.home.ui.mapper.IntensityLevelUiToPresentationMapper
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.notification.HomeMessagePresenter

@Suppress("LongParameterList")
class HomeDependencies(
    val homeViewModel: HomeViewModel,
    val homeViewStateUiMapper: HomeViewStateUiMapper,
    val skyAppearanceUiMapper: SkyAppearanceUiMapper,
    val intensityLevelUiToPresentationMapper: IntensityLevelUiToPresentationMapper,
    val timeUiFormatter: TimeUiFormatter,
    val homeMessagePresenter: HomeMessagePresenter,
    homeNavigationMapper: NavigationEventDestinationMapper<HomePresentationNavigationEvent>,
    homeNotificationMapper: NotificationUiMapper<HomePresentationNotification>
) : BaseComposeHolder<HomeViewState, HomePresentationNotification>(
    homeViewModel,
    homeNavigationMapper,
    homeNotificationMapper
)
