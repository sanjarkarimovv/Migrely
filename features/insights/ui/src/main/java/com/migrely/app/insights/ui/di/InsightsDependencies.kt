package com.migrely.app.insights.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.insights.presentation.model.InsightsPresentationNotification
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.presentation.navigation.InsightsPresentationNavigationEvent
import com.migrely.app.insights.presentation.viewmodel.InsightsViewModel
import com.migrely.app.insights.ui.mapper.InsightsShownStateResolver
import com.migrely.app.insights.ui.mapper.InsightsViewStateUiMapper

class InsightsDependencies(
    val insightsViewModel: InsightsViewModel,
    val insightsViewStateUiMapper: InsightsViewStateUiMapper,
    val insightsShownStateResolver: InsightsShownStateResolver,
    navigationMapper: NavigationEventDestinationMapper<InsightsPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<InsightsPresentationNotification>
) : BaseComposeHolder<InsightsViewState, InsightsPresentationNotification>(
    insightsViewModel,
    navigationMapper,
    notificationMapper
)
