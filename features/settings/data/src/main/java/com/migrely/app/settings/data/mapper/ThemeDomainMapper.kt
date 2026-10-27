package com.migrely.app.settings.data.mapper

import com.migrely.app.datasource.preferences.model.ThemeDataModel
import com.migrely.app.settings.domain.model.ThemeDomainModel

class ThemeDomainMapper {
    fun toDomain(theme: ThemeDataModel) = when (theme) {
        ThemeDataModel.Dark -> ThemeDomainModel.Dark
        ThemeDataModel.Light -> ThemeDomainModel.Light
        ThemeDataModel.System -> ThemeDomainModel.System
    }
}
