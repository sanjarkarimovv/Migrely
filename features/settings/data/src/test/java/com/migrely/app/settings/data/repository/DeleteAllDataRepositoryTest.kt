package com.migrely.app.settings.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.datasource.UnsavedMarksDataSource
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSource
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeleteAllDataRepositoryTest {
    private val reminderScheduleDataSource: ReminderScheduleDataSource = mock()
    private val notificationDataSource: NotificationDataSource = mock()
    private val backupDataSource: BackupDataSource = mock()
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val unsavedMarksDataSource: UnsavedMarksDataSource = mock()
    private val backupFileDataSource: BackupFileDataSource = mock()
    private val reportDocumentDataSource: ReportDocumentDataSource = mock()
    private val premiumCheckScheduleDataSource: PremiumCheckScheduleDataSource = mock()
    private val classUnderTest = DeleteAllDataRepository(
        reminderScheduleDataSource,
        notificationDataSource,
        backupDataSource,
        preferencesDataSource,
        unsavedMarksDataSource,
        backupFileDataSource,
        reportDocumentDataSource,
        premiumCheckScheduleDataSource
    )

    @Test
    fun `When each step runs then its datasources do it`() {
        listOf(
            classUnderTest.cancelReminders(),
            classUnderTest.deleteRecords(),
            classUnderTest.deletePreferences(),
            classUnderTest.deleteFiles()
        ).forEach { assertEquals(SettingsWriteResultDomainModel.Done, it) }

        inOrder(
            reminderScheduleDataSource,
            premiumCheckScheduleDataSource,
            notificationDataSource
        ) {
            verify(reminderScheduleDataSource).cancelAll()
            verify(premiumCheckScheduleDataSource).cancel()
            verify(notificationDataSource).cancelAll()
        }
        verify(backupDataSource).deleteAll()
        verify(preferencesDataSource).clear()
        inOrder(backupFileDataSource, reportDocumentDataSource) {
            verify(backupFileDataSource).deleteAll()
            verify(reportDocumentDataSource).deleteAll()
        }
    }

    @Test
    fun `Given a datasource fails then that step failed, as a value`() {
        whenever(reminderScheduleDataSource.cancelAll()).doThrow(DatabaseDataException())
        whenever(backupDataSource.deleteAll()).doThrow(DatabaseDataException())
        whenever(preferencesDataSource.clear()).doThrow(FileDataException())
        whenever(backupFileDataSource.deleteAll()).doThrow(FileDataException())

        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.cancelReminders())
        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.deleteRecords())
        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.deletePreferences())
        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.deleteFiles())
        verify(notificationDataSource, never()).cancelAll()
        verify(reportDocumentDataSource, never()).deleteAll()
    }

    @Test
    fun `When the settings are deleted then the marks this process could not save go too`() {
        assertEquals(SettingsWriteResultDomainModel.Done, classUnderTest.deletePreferences())

        // As on a new install in this process too (#278, D197): no unsaved paywall, rating or safety-card mark is left.
        inOrder(preferencesDataSource, unsavedMarksDataSource) {
            verify(preferencesDataSource).clear()
            verify(unsavedMarksDataSource).clear()
        }
    }

    @Test
    fun `Given the settings cannot be deleted then the unsaved marks stay, as stored ones do`() {
        whenever(preferencesDataSource.clear()).doThrow(FileDataException())

        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.deletePreferences())

        verify(unsavedMarksDataSource, never()).clear()
    }
}
