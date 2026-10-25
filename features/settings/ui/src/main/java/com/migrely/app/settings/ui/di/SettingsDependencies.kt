package com.migrely.app.settings.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent
import com.migrely.app.settings.presentation.viewmodel.SettingsViewModel
import com.migrely.app.settings.ui.mapper.SettingsEventPresenter
import com.migrely.app.settings.ui.mapper.SettingsViewStateUiMapper
import com.migrely.app.settings.ui.mapper.ThemeUiToPresentationMapper

class SettingsDependencies(
    val settingsViewModel: SettingsViewModel,
    val settingsViewStateUiMapper: SettingsViewStateUiMapper,
    val themeUiToPresentationMapper: ThemeUiToPresentationMapper,
    val settingsEventPresenter: SettingsEventPresenter,
    navigationMapper: NavigationEventDestinationMapper<SettingsPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<SettingsPresentationNotification>
) : BaseComposeHolder<SettingsViewState, SettingsPresentationNotification>(
    settingsViewModel,
    navigationMapper,
    notificationMapper
)
