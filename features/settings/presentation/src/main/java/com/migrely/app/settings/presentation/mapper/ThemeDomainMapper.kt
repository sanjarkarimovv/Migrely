package com.migrely.app.settings.presentation.mapper

import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.presentation.model.ThemePresentationModel

class ThemeDomainMapper {
    fun toDomain(theme: ThemePresentationModel) = when (theme) {
        ThemePresentationModel.Dark -> ThemeDomainModel.Dark
        ThemePresentationModel.Light -> ThemeDomainModel.Light
        ThemePresentationModel.System -> ThemeDomainModel.System
    }
}
