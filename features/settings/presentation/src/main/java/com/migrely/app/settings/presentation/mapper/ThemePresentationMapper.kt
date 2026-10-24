package com.migrely.app.settings.presentation.mapper

import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.presentation.model.ThemePresentationModel

class ThemePresentationMapper {
    fun toPresentation(theme: ThemeDomainModel) = when (theme) {
        ThemeDomainModel.Dark -> ThemePresentationModel.Dark
        ThemeDomainModel.Light -> ThemePresentationModel.Light
        ThemeDomainModel.System -> ThemePresentationModel.System
    }
}
