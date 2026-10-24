package com.migrely.app.settings.data.mapper

import com.migrely.app.datasource.preferences.model.ThemeDataModel
import com.migrely.app.settings.domain.model.ThemeDomainModel

class ThemeDataMapper {
    fun toData(theme: ThemeDomainModel) = when (theme) {
        ThemeDomainModel.Dark -> ThemeDataModel.Dark
        ThemeDomainModel.Light -> ThemeDataModel.Light
        ThemeDomainModel.System -> ThemeDataModel.System
    }
}
