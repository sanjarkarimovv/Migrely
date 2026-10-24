package com.migrely.app.settings.data.repository

import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.settings.data.mapper.ThemeDataMapper
import com.migrely.app.settings.data.mapper.ThemeDomainMapper
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.repository.GetSettingsRepository
import com.migrely.app.settings.domain.repository.GetThemeRepository
import com.migrely.app.settings.domain.repository.SetPeriodTrackingRepository
import com.migrely.app.settings.domain.repository.SetThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val preferencesDataSource: PreferencesDataSource,
    private val appInfoDataSource: AppInfoDataSource,
    private val themeDomainMapper: ThemeDomainMapper,
    private val themeDataMapper: ThemeDataMapper
) : GetSettingsRepository,
    SetPeriodTrackingRepository,
    SetThemeRepository,
    GetThemeRepository {
    override fun settings() = SettingsDomainModel(
        periodTracking = preferencesDataSource.periodTracking(),
        theme = themeDomainMapper.toDomain(preferencesDataSource.theme()),
        version = appInfoDataSource.versionName()
    )

    override fun setPeriodTracking(on: Boolean): SettingsWriteResultDomainModel = write {
        preferencesDataSource.setPeriodTracking(on)
    }

    override fun setTheme(theme: ThemeDomainModel): SettingsWriteResultDomainModel = write {
        preferencesDataSource.setTheme(themeDataMapper.toData(theme))
    }

    override fun theme(): Flow<ThemeDomainModel> =
        preferencesDataSource.themeChanges().map(themeDomainMapper::toDomain)

    private inline fun write(block: () -> Unit) = try {
        block()
        SettingsWriteResultDomainModel.Done
    } catch (_: DataException) {
        SettingsWriteResultDomainModel.Failed
    }
}
