package com.migrely.app.settings.presentation.model

sealed interface SettingsViewState {
    data object Loading : SettingsViewState

    data class Ready(val settings: SettingsPresentationModel) : SettingsViewState
}
