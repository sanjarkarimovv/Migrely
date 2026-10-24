package com.migrely.app.history.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.history.presentation.model.HistoryPresentationNotification
import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.history.presentation.viewmodel.HistoryViewModel
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.mapper.HistoryMessageBehaviourUiMapper
import com.migrely.app.history.ui.mapper.HistoryViewStateUiMapper
import com.migrely.app.history.ui.notification.HistoryMessagePresenter

@Suppress("LongParameterList")
class HistoryDependencies(
    val historyViewModel: HistoryViewModel,
    val historyViewStateUiMapper: HistoryViewStateUiMapper,
    val historyCalendarMappers: HistoryCalendarMappers,
    val historyMessagePresenter: HistoryMessagePresenter,
    val historyMessageBehaviourUiMapper: HistoryMessageBehaviourUiMapper,
    historyNavigationMapper: NavigationEventDestinationMapper<HistoryPresentationNavigationEvent>,
    historyNotificationMapper: NotificationUiMapper<HistoryPresentationNotification>
) : BaseComposeHolder<HistoryViewState, HistoryPresentationNotification>(
    historyViewModel,
    historyNavigationMapper,
    historyNotificationMapper
)
