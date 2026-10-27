package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.domain.usecase.SetThemeUseCase
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.mapper.ThemeDomainMapper
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.model.ThemePresentationModel
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent

/** Settings (BRD SET-F1, D70, D79, D93–D97), reached from home's header. */
@Suppress("TooManyFunctions")
class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setPeriodTrackingUseCase: SetPeriodTrackingUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val settingsPresentationMapper: SettingsPresentationMapper,
    private val themeDomainMapper: ThemeDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<SettingsViewState, SettingsPresentationNotification>(useCaseExecutor) {
    fun onEnter() = load()

    /** The screen shows again, after a rotation for example: the settings as they are. */
    fun onResume() = load()

    fun onBackAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnBack)

    fun onRemindersAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnReminders)

    fun onYourDataAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnYourData)

    fun onWarningSignsAction() =
        emitNavigationEvent(SettingsPresentationNavigationEvent.OnWarningSigns)

    fun onAboutMigrelyAction() =
        emitNavigationEvent(SettingsPresentationNavigationEvent.OnAboutMigrely)

    fun onPrivacyAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnPrivacy)

    fun onLicencesAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnLicences)

    /** Opens the confirmation; nothing is deleted from here (BRD D96). */
    fun onDeleteAllDataAction() =
        emitNavigationEvent(SettingsPresentationNavigationEvent.OnDeleteAllData)

    fun onPeriodTrackingAction(on: Boolean) = setPeriodTrackingUseCase(
        value = on,
        onResult = ::saved
    )

    /** Every screen turns to the new theme at once, through the app's own theme (BRD D94). */
    fun onThemeAction(theme: ThemePresentationModel) = setThemeUseCase(
        value = themeDomainMapper.toDomain(theme),
        onResult = ::saved
    )

    private fun saved(result: SettingsWriteResultDomainModel) {
        when (result) {
            SettingsWriteResultDomainModel.Done -> Unit
            SettingsWriteResultDomainModel.Failed -> notify(
                SettingsPresentationNotification.NotSaved
            )
        }
        load()
    }

    private fun load() = getSettingsUseCase(
        onResult = {
            updateViewState(SettingsViewState.Ready(settingsPresentationMapper.toPresentation(it)))
        }
    )
}
