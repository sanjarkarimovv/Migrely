package com.migrely.app.settings.data.repository

import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.GetSettingsRepository
import com.migrely.app.settings.domain.repository.SetPeriodTrackingRepository

class SettingsRepository(private val preferencesDataSource: PreferencesDataSource) :
    GetSettingsRepository,
    SetPeriodTrackingRepository {
    override fun settings() =
        SettingsDomainModel(periodTracking = preferencesDataSource.periodTracking())

    override fun setPeriodTracking(on: Boolean): SettingsWriteResultDomainModel = try {
        preferencesDataSource.setPeriodTracking(on)
        SettingsWriteResultDomainModel.Done
    } catch (_: DataException) {
        SettingsWriteResultDomainModel.Failed
    }
}
