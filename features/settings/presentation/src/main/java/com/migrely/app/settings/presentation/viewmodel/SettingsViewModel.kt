package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent

/** Settings (BRD D70, D79): where the app's own settings live, reached from home's header. */
class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setPeriodTrackingUseCase: SetPeriodTrackingUseCase,
    private val settingsPresentationMapper: SettingsPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<SettingsViewState, SettingsPresentationNotification>(useCaseExecutor) {
    fun onEnter() = load()

    fun onBackAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnBack)

    fun onRemindersAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnReminders)

    fun onYourDataAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnYourData)

    fun onPeriodTrackingAction(on: Boolean) = setPeriodTrackingUseCase(
        value = on,
        onResult = { result ->
            when (result) {
                SettingsWriteResultDomainModel.Done -> Unit
                SettingsWriteResultDomainModel.Failed -> notify(
                    SettingsPresentationNotification.NotSaved
                )
            }
            load()
        },
        onException = {
            notify(SettingsPresentationNotification.NotSaved)
            load()
        }
    )

    private fun load() = getSettingsUseCase(
        onResult = {
            updateViewState(SettingsViewState.Ready(settingsPresentationMapper.toPresentation(it)))
        }
    )
}
