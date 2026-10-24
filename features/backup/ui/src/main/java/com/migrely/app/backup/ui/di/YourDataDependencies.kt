package com.migrely.app.backup.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent
import com.migrely.app.backup.presentation.viewmodel.YourDataViewModel
import com.migrely.app.backup.ui.mapper.YourDataEventPresenter
import com.migrely.app.backup.ui.mapper.YourDataViewStateUiMapper

class YourDataDependencies(
    val yourDataViewModel: YourDataViewModel,
    val yourDataViewStateUiMapper: YourDataViewStateUiMapper,
    val yourDataEventPresenter: YourDataEventPresenter,
    navigationMapper: NavigationEventDestinationMapper<YourDataPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<YourDataPresentationNotification>
) : BaseComposeHolder<YourDataViewState, YourDataPresentationNotification>(
    yourDataViewModel,
    navigationMapper,
    notificationMapper
)
