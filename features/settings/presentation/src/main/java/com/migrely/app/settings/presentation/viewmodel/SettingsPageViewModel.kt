package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.settings.presentation.model.SettingsPagePresentationModel
import com.migrely.app.settings.presentation.model.SettingsPagePresentationNotification
import com.migrely.app.settings.presentation.model.SettingsPageViewState
import com.migrely.app.settings.presentation.navigation.SettingsPagePresentationNavigationEvent

/** Warning signs, About Migrely, Privacy and Open-source licences (BRD D95, D97): fixed text, and a way back. */
class SettingsPageViewModel(useCaseExecutor: UseCaseExecutor) :
    BaseViewModel<SettingsPageViewState, SettingsPagePresentationNotification>(useCaseExecutor) {
    fun onBackAction(page: SettingsPagePresentationModel) =
        emitNavigationEvent(SettingsPagePresentationNavigationEvent.OnBack(page))
}
