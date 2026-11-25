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

/**
 * Settings (BRD SET-F1, SET-F2, D70, D79, D93–D97, D185), reached from home's header. The one thing kept is the number
 * of the latest read, so a read made before an attack started never shows "Share and rate" when it answers last.
 */
@Suppress("TooManyFunctions")
class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setPeriodTrackingUseCase: SetPeriodTrackingUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val settingsPresentationMapper: SettingsPresentationMapper,
    private val themeDomainMapper: ThemeDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<SettingsViewState, SettingsPresentationNotification>(useCaseExecutor) {
    /** The number of the latest read; reads are made and answered on the main thread. */
    private var latestRead = 0

    fun onEnter() = load()

    /** The screen shows again, after a rotation for example: the settings as they are. */
    fun onResume() = load()

    /** An attack started or ended while Settings shows: "Share and rate" follows (BRD EC-SHR-13). */
    fun onAttackChanged() = load()

    /** Nothing is written, rewarded or counted (BRD D185). */
    fun onShareAction() = notify(SettingsPresentationNotification.OpenShareSheet)

    fun onRateAction() = notify(SettingsPresentationNotification.OpenStorePage)

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

    private fun load() {
        val read = ++latestRead
        getSettingsUseCase(
            onResult = {
                if (read == latestRead) {
                    updateViewState(
                        SettingsViewState.Ready(settingsPresentationMapper.toPresentation(it))
                    )
                }
            }
        )
    }
}
