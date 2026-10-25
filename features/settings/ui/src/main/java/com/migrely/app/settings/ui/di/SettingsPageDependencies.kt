package com.migrely.app.settings.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.settings.presentation.model.SettingsPagePresentationNotification
import com.migrely.app.settings.presentation.model.SettingsPageViewState
import com.migrely.app.settings.presentation.navigation.SettingsPagePresentationNavigationEvent
import com.migrely.app.settings.presentation.viewmodel.SettingsPageViewModel

/** Warning signs, About Migrely, Privacy and Open-source licences. */
class SettingsPageDependencies(
    val settingsPageViewModel: SettingsPageViewModel,
    navigationMapper: NavigationEventDestinationMapper<SettingsPagePresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<SettingsPagePresentationNotification>
) : BaseComposeHolder<SettingsPageViewState, SettingsPagePresentationNotification>(
    settingsPageViewModel,
    navigationMapper,
    notificationMapper
)
