package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.ThemePresentationModel
import com.migrely.app.settings.ui.model.ThemeUiModel

class ThemeUiMapper {
    fun toUi(theme: ThemePresentationModel) = when (theme) {
        ThemePresentationModel.Dark -> ThemeUiModel.Dark
        ThemePresentationModel.Light -> ThemeUiModel.Light
        ThemePresentationModel.System -> ThemeUiModel.System
    }
}
