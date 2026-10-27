package com.migrely.app.di

import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSource
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.settings.data.mapper.ThemeDataMapper
import com.migrely.app.settings.data.mapper.ThemeDomainMapper
import com.migrely.app.settings.data.repository.DeleteAllDataRepository
import com.migrely.app.settings.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Settings' repositories (change add-settings-and-safety). Each serves several single-function repository interfaces,
 * so the use cases take them directly.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object SettingsDataModule {
    @Provides
    fun providesSettingsRepository(
        preferencesDataSource: PreferencesDataSource,
        appInfoDataSource: AppInfoDataSource,
        attackDataSource: AttackDataSource
    ) = SettingsRepository(
        preferencesDataSource,
        appInfoDataSource,
        attackDataSource,
        ThemeDomainMapper(),
        ThemeDataMapper()
    )

    @Provides
    fun providesDeleteAllDataRepository(
        reminderScheduleDataSource: ReminderScheduleDataSource,
        notificationDataSource: NotificationDataSource,
        backupDataSource: BackupDataSource,
        preferencesDataSource: PreferencesDataSource,
        backupFileDataSource: BackupFileDataSource,
        reportDocumentDataSource: ReportDocumentDataSource,
        premiumCheckScheduleDataSource: PremiumCheckScheduleDataSource
    ) = DeleteAllDataRepository(
        reminderScheduleDataSource,
        notificationDataSource,
        backupDataSource,
        preferencesDataSource,
        backupFileDataSource,
        reportDocumentDataSource,
        premiumCheckScheduleDataSource
    )
}
