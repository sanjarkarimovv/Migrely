package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.ui.model.SettingsUiModel

class SettingsViewStateUiMapper(private val themeUiMapper: ThemeUiMapper) {
    /** Null while loading. */
    fun toUi(viewState: SettingsViewState): SettingsUiModel? = when (viewState) {
        SettingsViewState.Loading -> null
        is SettingsViewState.Ready -> SettingsUiModel(
            periodTracking = viewState.settings.periodTracking,
            theme = themeUiMapper.toUi(viewState.settings.theme),
            version = viewState.settings.version
        )
    }
}
