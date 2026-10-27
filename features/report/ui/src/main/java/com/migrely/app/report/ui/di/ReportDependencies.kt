package com.migrely.app.report.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.report.presentation.model.ReportPresentationNotification
import com.migrely.app.report.presentation.model.ReportViewState
import com.migrely.app.report.presentation.navigation.ReportPresentationNavigationEvent
import com.migrely.app.report.presentation.viewmodel.ReportViewModel
import com.migrely.app.report.ui.mapper.ReportEventPresenter
import com.migrely.app.report.ui.mapper.ReportMessageBehaviourUiMapper
import com.migrely.app.report.ui.mapper.ReportPeriodPresentationMapper
import com.migrely.app.report.ui.mapper.ReportViewStateUiMapper

class ReportDependencies(
    val reportViewModel: ReportViewModel,
    val reportViewStateUiMapper: ReportViewStateUiMapper,
    val reportPeriodPresentationMapper: ReportPeriodPresentationMapper,
    val reportEventPresenter: ReportEventPresenter,
    val reportMessageBehaviourUiMapper: ReportMessageBehaviourUiMapper,
    navigationMapper: NavigationEventDestinationMapper<ReportPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<ReportPresentationNotification>
) : BaseComposeHolder<ReportViewState, ReportPresentationNotification>(
    reportViewModel,
    navigationMapper,
    notificationMapper
)
