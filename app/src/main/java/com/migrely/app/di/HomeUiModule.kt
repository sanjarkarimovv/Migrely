package com.migrely.app.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NoNotificationUiMapper
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.home.presentation.viewmodel.HomeViewModel
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.home.ui.mapper.HomeViewStateUiMapper
import com.migrely.app.navigation.mapper.HomeNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HomeUiModule {
    @Provides
    fun providesHomeViewStateUiMapper(): HomeViewStateUiMapper = HomeViewStateUiMapper()

    @Provides
    fun providesHomeNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<HomePresentationNavigationEvent> =
        HomeNavigationEventDestinationMapper()

    @Provides
    fun providesHomeDependencies(
        homeViewModel: HomeViewModel,
        homeViewStateUiMapper: HomeViewStateUiMapper,
        homeNavigationMapper:
        @JvmSuppressWildcards NavigationEventDestinationMapper<HomePresentationNavigationEvent>
    ): HomeDependencies = HomeDependencies(
        homeViewModel = homeViewModel,
        homeViewStateUiMapper = homeViewStateUiMapper,
        homeNavigationMapper = homeNavigationMapper,
        homeNotificationMapper = NoNotificationUiMapper()
    )
}
