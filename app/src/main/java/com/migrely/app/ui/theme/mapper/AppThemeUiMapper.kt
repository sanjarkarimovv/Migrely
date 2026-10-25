package com.migrely.app.ui.theme.mapper

import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.ui.theme.model.AppThemeUiModel

/** The settings feature's theme, as the app draws it. */
class AppThemeUiMapper {
    fun toUi(theme: ThemeDomainModel) = when (theme) {
        ThemeDomainModel.Dark -> AppThemeUiModel.Dark
        ThemeDomainModel.Light -> AppThemeUiModel.Light
        ThemeDomainModel.System -> AppThemeUiModel.System
    }
}
