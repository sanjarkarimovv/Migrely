package com.migrely.app.settings.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSource
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.CancelRemindersRepository
import com.migrely.app.settings.domain.repository.DeleteFilesRepository
import com.migrely.app.settings.domain.repository.DeletePreferencesRepository
import com.migrely.app.settings.domain.repository.DeleteRecordsRepository

/** The steps of "Delete all data" (BRD D96); `DeleteAllDataUseCase` decides their order. */
@Suppress("LongParameterList")
class DeleteAllDataRepository(
    private val reminderScheduleDataSource: ReminderScheduleDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val backupDataSource: BackupDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val backupFileDataSource: BackupFileDataSource,
    private val reportDocumentDataSource: ReportDocumentDataSource,
    private val premiumCheckScheduleDataSource: PremiumCheckScheduleDataSource
) : CancelRemindersRepository,
    DeleteRecordsRepository,
    DeletePreferencesRepository,
    DeleteFilesRepository {
    /** Every planned background run: reminders, notifications, and the checks of Premium (I2). */
    override fun cancelReminders() = step {
        reminderScheduleDataSource.cancelAll()
        premiumCheckScheduleDataSource.cancel()
        notificationDataSource.cancelAll()
    }

    override fun deleteRecords() = step { backupDataSource.deleteAll() }

    override fun deletePreferences() = step { preferencesDataSource.clear() }

    override fun deleteFiles() = step {
        backupFileDataSource.deleteAll()
        reportDocumentDataSource.deleteAll()
    }

    private inline fun step(block: () -> Unit) = try {
        block()
        SettingsWriteResultDomainModel.Done
    } catch (_: DataException) {
        SettingsWriteResultDomainModel.Failed
    }
}
