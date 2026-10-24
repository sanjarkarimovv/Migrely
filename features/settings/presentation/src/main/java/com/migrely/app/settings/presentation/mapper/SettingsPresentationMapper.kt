package com.migrely.app.settings.presentation.mapper

import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.presentation.model.SettingsPresentationModel

class SettingsPresentationMapper {
    fun toPresentation(settings: SettingsDomainModel) =
        SettingsPresentationModel(periodTracking = settings.periodTracking)
}
