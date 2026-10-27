package com.migrely.app.di

import android.content.Context
import android.text.format.DateFormat
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.home.presentation.viewmodel.HomeViewModel
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.HomeNotificationUiMapper
import com.migrely.app.home.ui.mapper.HomeViewStateUiMapper
import com.migrely.app.home.ui.mapper.IntensityLevelUiToPresentationMapper
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import com.migrely.app.locale.FormattingLocales
import com.migrely.app.navigation.mapper.HomeNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.ZoneId

@Module
@InstallIn(SingletonComponent::class)
internal object HomeUiModule {
    @Provides
    fun providesHomeNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<HomePresentationNavigationEvent> =
        HomeNavigationEventDestinationMapper()

    @Provides
    fun providesTimeUiFormatter(
        @ApplicationContext context: Context,
        formattingLocales: FormattingLocales
    ) = TimeUiFormatter(
        locale = { formattingLocales.of(context.resources.configuration) },
        is24HourFormat = { DateFormat.is24HourFormat(context) },
        deviceZone = { ZoneId.systemDefault() },
        bestDateTimePattern = { locale, skeleton ->
            DateFormat.getBestDateTimePattern(locale, skeleton)
        }
    )

    @Provides
    fun providesHomeDependencies(
        homeViewModel: HomeViewModel,
        timeUiFormatter: TimeUiFormatter,
        homeNavigationMapper:
        @JvmSuppressWildcards NavigationEventDestinationMapper<HomePresentationNavigationEvent>
    ): HomeDependencies {
        // The mapper and the screen must share one presenter so notifications reach this screen's snackbar.
        val homeMessagePresenter = HomeMessagePresenter()
        return HomeDependencies(
            homeViewModel = homeViewModel,
            homeViewStateUiMapper = HomeViewStateUiMapper(),
            skyAppearanceUiMapper = SkyAppearanceUiMapper(),
            intensityLevelUiToPresentationMapper = IntensityLevelUiToPresentationMapper(),
            timeUiFormatter = timeUiFormatter,
            homeMessagePresenter = homeMessagePresenter,
            homeMessageBehaviourUiMapper = HomeMessageBehaviourUiMapper(),
            homeNavigationMapper = homeNavigationMapper,
            homeNotificationMapper = HomeNotificationUiMapper(homeMessagePresenter)
        )
    }
}
