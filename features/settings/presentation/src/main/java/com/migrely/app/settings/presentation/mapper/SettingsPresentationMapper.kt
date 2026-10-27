package com.migrely.app.settings.presentation.mapper

import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.presentation.model.SettingsPresentationModel

class SettingsPresentationMapper(private val themePresentationMapper: ThemePresentationMapper) {
    fun toPresentation(settings: SettingsDomainModel) = SettingsPresentationModel(
        periodTracking = settings.periodTracking,
        theme = themePresentationMapper.toPresentation(settings.theme),
        version = settings.version
    )
}
