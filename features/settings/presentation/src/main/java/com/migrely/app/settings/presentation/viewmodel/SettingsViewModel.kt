package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.settings.domain.model.DeleteAllDataResultDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.domain.usecase.SetThemeUseCase
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.mapper.ThemeDomainMapper
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.model.ThemePresentationModel
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent

/**
 * Settings (BRD SET-F1, D70, D79, D93–D97), reached from home's header.
 *
 * Deleting all data can outlast a rotation, so this view model lives as long as the app and keeps, only for delivery,
 * whether the deletion is going on and how it ended: finished, or failed and not yet shown. A screen that comes back
 * sees the progress, is told of the failure it missed, or is sent on to the restart (ARCHITECTURE section 13).
 */
@Suppress("TooManyFunctions")
class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setPeriodTrackingUseCase: SetPeriodTrackingUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val deleteAllDataUseCase: DeleteAllDataUseCase,
    private val settingsPresentationMapper: SettingsPresentationMapper,
    private val themeDomainMapper: ThemeDomainMapper,
    private val deleteConfirmationResolver: DeleteConfirmationResolver,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<SettingsViewState, SettingsPresentationNotification>(useCaseExecutor) {
    private var deleting = false
    private var allDeleted = false
    private var failureNotShown = false

    fun onEnter() = load()

    /** The screen shows again, after a rotation for example: the settings as they are, and an outcome it missed. */
    fun onResume() = load()

    /** The screen showed that deleting failed, so it is not told again. */
    fun onDeleteFailureShownAction() {
        failureNotShown = false
    }

    fun onBackAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnBack)

    fun onRemindersAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnReminders)

    fun onYourDataAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnYourData)

    fun onWarningSignsAction() =
        emitNavigationEvent(SettingsPresentationNavigationEvent.OnWarningSigns)

    fun onAboutMigrelyAction() =
        emitNavigationEvent(SettingsPresentationNavigationEvent.OnAboutMigrely)

    fun onPrivacyAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnPrivacy)

    fun onLicencesAction() = emitNavigationEvent(SettingsPresentationNavigationEvent.OnLicences)

    fun onPeriodTrackingAction(on: Boolean) = setPeriodTrackingUseCase(
        value = on,
        onResult = ::saved
    )

    /** Every screen turns to the new theme at once, through the app's own theme (BRD D94). */
    fun onThemeAction(theme: ThemePresentationModel) = setThemeUseCase(
        value = themeDomainMapper.toDomain(theme),
        onResult = ::saved
    )

    /** The user confirmed after typing [typed]; only the confirmation word, exactly, deletes (BRD D96). */
    fun onDeleteAllDataAction(typed: String) {
        if (!deleteConfirmationResolver.resolve(typed) || deleting || allDeleted) return
        deleting = true
        failureNotShown = false
        load()
        deleteAllDataUseCase(
            onResult = { result ->
                when (result) {
                    DeleteAllDataResultDomainModel.Deleted -> {
                        // The progress stays until the app starts again.
                        allDeleted = true
                        emitNavigationEvent(SettingsPresentationNavigationEvent.OnAllDataDeleted)
                    }

                    DeleteAllDataResultDomainModel.Failed -> notDeleted()
                }
            },
            onException = { notDeleted() }
        )
    }

    /** Kept until the screen takes it, so a rotation never loses it. */
    private fun notDeleted() {
        deleting = false
        failureNotShown = true
        load()
    }

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
            updateViewState(
                SettingsViewState.Ready(settingsPresentationMapper.toPresentation(it), deleting)
            )
            if (failureNotShown) notify(SettingsPresentationNotification.NotDeleted)
            if (allDeleted) {
                emitNavigationEvent(
                    SettingsPresentationNavigationEvent.OnAllDataDeleted
                )
            }
        }
    )
}
