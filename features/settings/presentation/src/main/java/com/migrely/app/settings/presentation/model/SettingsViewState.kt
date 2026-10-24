package com.migrely.app.settings.presentation.model

/** Settings lists fixed items, so its only state is being shown. */
sealed interface SettingsViewState {
    data object Ready : SettingsViewState
}
