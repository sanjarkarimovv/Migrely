package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.ui.model.SettingsUiModel

class SettingsViewStateUiMapper {
    /** Null while loading. */
    fun toUi(viewState: SettingsViewState): SettingsUiModel? = when (viewState) {
        SettingsViewState.Loading -> null
        is SettingsViewState.Ready -> SettingsUiModel(
            periodTracking = viewState.settings.periodTracking
        )
    }
}
