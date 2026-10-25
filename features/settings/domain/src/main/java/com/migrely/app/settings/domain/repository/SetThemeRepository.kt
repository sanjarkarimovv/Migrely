package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel

interface SetThemeRepository {
    fun setTheme(theme: ThemeDomainModel): SettingsWriteResultDomainModel
}
