package com.migrely.app.settings.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.model.DeleteAllDataPresentationNotification
import com.migrely.app.settings.presentation.model.DeleteAllDataViewState
import com.migrely.app.settings.presentation.navigation.DeleteAllDataPresentationNavigationEvent
import com.migrely.app.settings.presentation.viewmodel.DeleteAllDataViewModel
import com.migrely.app.settings.ui.mapper.DeleteAllDataEventPresenter
import com.migrely.app.settings.ui.mapper.DeleteAllDataViewStateUiMapper
import com.migrely.app.settings.ui.mapper.DeleteProgressUiResolver

class DeleteAllDataDependencies(
    val deleteAllDataViewModel: DeleteAllDataViewModel,
    val deleteAllDataViewStateUiMapper: DeleteAllDataViewStateUiMapper,
    val deleteConfirmationResolver: DeleteConfirmationResolver,
    val deleteProgressUiResolver: DeleteProgressUiResolver,
    val deleteAllDataEventPresenter: DeleteAllDataEventPresenter,
    navigationMapper: NavigationEventDestinationMapper<DeleteAllDataPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<DeleteAllDataPresentationNotification>
) : BaseComposeHolder<DeleteAllDataViewState, DeleteAllDataPresentationNotification>(
    deleteAllDataViewModel,
    navigationMapper,
    notificationMapper
)
