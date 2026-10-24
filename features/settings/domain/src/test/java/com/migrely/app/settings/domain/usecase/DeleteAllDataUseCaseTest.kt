package com.migrely.app.settings.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.DeleteAllDataResultDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.CancelRemindersRepository
import com.migrely.app.settings.domain.repository.DeleteFilesRepository
import com.migrely.app.settings.domain.repository.DeletePreferencesRepository
import com.migrely.app.settings.domain.repository.DeleteRecordsRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class DeleteAllDataUseCaseTest {
    private val cancelRemindersRepository: CancelRemindersRepository = mock()
    private val deleteRecordsRepository: DeleteRecordsRepository = mock()
    private val deletePreferencesRepository: DeletePreferencesRepository = mock()
    private val deleteFilesRepository: DeleteFilesRepository = mock()
    private val classUnderTest = DeleteAllDataUseCase(
        cancelRemindersRepository,
        deleteRecordsRepository,
        deletePreferencesRepository,
        deleteFilesRepository,
        CoroutineContextProvider.Default
    )

    @Before
    fun setUp() {
        given(cancelRemindersRepository.cancelReminders()).willReturn(Done)
        given(deleteRecordsRepository.deleteRecords()).willReturn(Done)
        given(deletePreferencesRepository.deletePreferences()).willReturn(Done)
        given(deleteFilesRepository.deleteFiles()).willReturn(Done)
    }

    private fun deleteAll() = classUnderTest.executeInBackground(Unit)

    @Test
    fun `When deleting all data then records, files, reminders, and the settings last`() {
        assertEquals(DeleteAllDataResultDomainModel.Deleted, deleteAll())

        inOrder(
            deleteRecordsRepository,
            deleteFilesRepository,
            cancelRemindersRepository,
            deletePreferencesRepository
        ) {
            verify(deleteRecordsRepository).deleteRecords()
            verify(deleteFilesRepository).deleteFiles()
            verify(cancelRemindersRepository).cancelReminders()
            verify(deletePreferencesRepository).deletePreferences()
        }
    }

    @Test
    fun `Given the records cannot be deleted then nothing else changes, reminders included`() {
        given(deleteRecordsRepository.deleteRecords()).willReturn(Failed)

        assertEquals(DeleteAllDataResultDomainModel.Failed, deleteAll())
        verify(deleteFilesRepository, never()).deleteFiles()
        verify(cancelRemindersRepository, never()).cancelReminders()
        verify(deletePreferencesRepository, never()).deletePreferences()
    }

    @Test
    fun `Given the files cannot be deleted then reminders and settings stay`() {
        given(deleteFilesRepository.deleteFiles()).willReturn(Failed)

        assertEquals(DeleteAllDataResultDomainModel.Failed, deleteAll())
        verify(cancelRemindersRepository, never()).cancelReminders()
        verify(deletePreferencesRepository, never()).deletePreferences()
    }

    @Test
    fun `Given a later step fails then failed, so the user can try again`() {
        given(cancelRemindersRepository.cancelReminders()).willReturn(Failed)
        assertEquals(DeleteAllDataResultDomainModel.Failed, deleteAll())
        verify(deletePreferencesRepository, never()).deletePreferences()

        given(cancelRemindersRepository.cancelReminders()).willReturn(Done)
        given(deletePreferencesRepository.deletePreferences()).willReturn(Failed)
        assertEquals(DeleteAllDataResultDomainModel.Failed, deleteAll())
    }

    private companion object {
        val Done = SettingsWriteResultDomainModel.Done
        val Failed = SettingsWriteResultDomainModel.Failed
    }
}
