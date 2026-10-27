package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsDomainModel

interface GetSettingsRepository {
    fun settings(): SettingsDomainModel
}
