package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent

/** Settings (BRD D70): where the app's own settings live, reached from home's header. */
class SettingsViewModel(useCaseExecutor: UseCaseExecutor) :
    BaseViewModel<SettingsViewState, SettingsPresentationNotification>(useCaseExecutor) {
    fun onEnter() = updateViewState(SettingsViewState.Ready)

    fun onBackAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnBack)

    fun onRemindersAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnReminders)

    fun onYourDataAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnYourData)
}
