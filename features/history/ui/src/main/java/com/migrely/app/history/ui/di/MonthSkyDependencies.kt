package com.migrely.app.history.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.history.presentation.model.MonthSkyPresentationNotification
import com.migrely.app.history.presentation.model.MonthSkyViewState
import com.migrely.app.history.presentation.navigation.MonthSkyPresentationNavigationEvent
import com.migrely.app.history.presentation.viewmodel.MonthSkyViewModel
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.mapper.MonthSkyViewStateUiMapper

/** The month card that `:app` puts into home's slot under today's card (design D1). */
class MonthSkyDependencies(
    val monthSkyViewModel: MonthSkyViewModel,
    val monthSkyViewStateUiMapper: MonthSkyViewStateUiMapper,
    val historyCalendarMappers: HistoryCalendarMappers,
    monthSkyNavigationMapper: NavigationEventDestinationMapper<MonthSkyPresentationNavigationEvent>,
    monthSkyNotificationMapper: NotificationUiMapper<MonthSkyPresentationNotification>
) : BaseComposeHolder<MonthSkyViewState, MonthSkyPresentationNotification>(
    monthSkyViewModel,
    monthSkyNavigationMapper,
    monthSkyNotificationMapper
)
