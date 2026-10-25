package com.migrely.app.di

import android.content.Context
import android.text.format.DateFormat
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.history.presentation.navigation.MonthSkyPresentationNavigationEvent
import com.migrely.app.history.presentation.viewmodel.HistoryViewModel
import com.migrely.app.history.presentation.viewmodel.MonthSkyViewModel
import com.migrely.app.history.ui.di.HistoryDependencies
import com.migrely.app.history.ui.di.MonthSkyDependencies
import com.migrely.app.history.ui.mapper.CalendarLayoutUiMapper
import com.migrely.app.history.ui.mapper.DaySkyAppearanceUiMapper
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.mapper.HistoryMessageBehaviourUiMapper
import com.migrely.app.history.ui.mapper.HistoryMonthUiMapper
import com.migrely.app.history.ui.mapper.HistoryNotificationUiMapper
import com.migrely.app.history.ui.mapper.HistoryTimeUiFormatter
import com.migrely.app.history.ui.mapper.HistoryViewStateUiMapper
import com.migrely.app.history.ui.mapper.MonthSkyNotificationUiMapper
import com.migrely.app.history.ui.mapper.MonthSkyViewStateUiMapper
import com.migrely.app.history.ui.notification.HistoryMessagePresenter
import com.migrely.app.navigation.mapper.HistoryNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.MonthSkyNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.ZoneId
import java.util.Locale

@Module
@InstallIn(SingletonComponent::class)
internal object HistoryUiModule {
    @Provides
    fun providesHistoryNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<HistoryPresentationNavigationEvent> =
        HistoryNavigationEventDestinationMapper()

    @Provides
    fun providesMonthSkyNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<MonthSkyPresentationNavigationEvent> =
        MonthSkyNavigationEventDestinationMapper()

    @Provides
    fun providesHistoryCalendarMappers(@ApplicationContext context: Context) =
        HistoryCalendarMappers(
            calendarLayoutUiMapper = CalendarLayoutUiMapper {
                Locale.getDefault()
            },
            daySkyAppearanceUiMapper = DaySkyAppearanceUiMapper(),
            historyTimeUiFormatter = HistoryTimeUiFormatter(
                locale = { Locale.getDefault() },
                is24HourFormat = { DateFormat.is24HourFormat(context) },
                deviceZone = { ZoneId.systemDefault() },
                bestDateTimePattern = { locale, skeleton ->
                    DateFormat.getBestDateTimePattern(locale, skeleton)
                }
            )
        )

    @Provides
    fun providesHistoryDependencies(
        historyViewModel: HistoryViewModel,
        historyCalendarMappers: HistoryCalendarMappers,
        historyNavigationMapper:
        @JvmSuppressWildcards NavigationEventDestinationMapper<HistoryPresentationNavigationEvent>
    ): HistoryDependencies {
        // The mapper and the screen must share one presenter so notifications reach this screen's snackbar.
        val historyMessagePresenter = HistoryMessagePresenter()
        return HistoryDependencies(
            historyViewModel = historyViewModel,
            historyViewStateUiMapper = HistoryViewStateUiMapper(HistoryMonthUiMapper()),
            historyCalendarMappers = historyCalendarMappers,
            historyMessagePresenter = historyMessagePresenter,
            historyMessageBehaviourUiMapper = HistoryMessageBehaviourUiMapper(),
            historyNavigationMapper = historyNavigationMapper,
            historyNotificationMapper = HistoryNotificationUiMapper(historyMessagePresenter)
        )
    }

    @Provides
    fun providesMonthSkyDependencies(
        monthSkyViewModel: MonthSkyViewModel,
        historyCalendarMappers: HistoryCalendarMappers,
        monthSkyNavigationMapper:
        @JvmSuppressWildcards NavigationEventDestinationMapper<MonthSkyPresentationNavigationEvent>
    ) = MonthSkyDependencies(
        monthSkyViewModel = monthSkyViewModel,
        monthSkyViewStateUiMapper = MonthSkyViewStateUiMapper(HistoryMonthUiMapper()),
        historyCalendarMappers = historyCalendarMappers,
        monthSkyNavigationMapper = monthSkyNavigationMapper,
        monthSkyNotificationMapper = MonthSkyNotificationUiMapper()
    )
}
