package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.ThemePresentationModel
import com.migrely.app.settings.ui.model.ThemeUiModel

class ThemeUiToPresentationMapper {
    fun toPresentation(theme: ThemeUiModel) = when (theme) {
        ThemeUiModel.Dark -> ThemePresentationModel.Dark
        ThemeUiModel.Light -> ThemePresentationModel.Light
        ThemeUiModel.System -> ThemePresentationModel.System
    }
}
