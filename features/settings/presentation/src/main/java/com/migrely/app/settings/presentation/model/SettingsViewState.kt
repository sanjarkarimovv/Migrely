package com.migrely.app.settings.presentation.model

sealed interface SettingsViewState {
    data object Loading : SettingsViewState

    /** [deleting] while all data is being deleted: the confirmation shows its progress and waits. */
    data class Ready(val settings: SettingsPresentationModel, val deleting: Boolean) :
        SettingsViewState
}
