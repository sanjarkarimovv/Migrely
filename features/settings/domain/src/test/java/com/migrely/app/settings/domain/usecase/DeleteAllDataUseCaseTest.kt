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

    @Test
    fun `When deleting all data then reminders first, then records, settings and files`() {
        assertEquals(
            DeleteAllDataResultDomainModel.Deleted,
            classUnderTest.executeInBackground(Unit)
        )

        inOrder(
            cancelRemindersRepository,
            deleteRecordsRepository,
            deletePreferencesRepository,
            deleteFilesRepository
        ) {
            verify(cancelRemindersRepository).cancelReminders()
            verify(deleteRecordsRepository).deleteRecords()
            verify(deletePreferencesRepository).deletePreferences()
            verify(deleteFilesRepository).deleteFiles()
        }
    }

    @Test
    fun `Given reminders cannot be cancelled then nothing is deleted`() {
        given(cancelRemindersRepository.cancelReminders()).willReturn(Failed)

        assertEquals(
            DeleteAllDataResultDomainModel.Failed,
            classUnderTest.executeInBackground(Unit)
        )
        verify(deleteRecordsRepository, never()).deleteRecords()
    }

    @Test
    fun `Given the records cannot be deleted then the settings and files stay`() {
        given(deleteRecordsRepository.deleteRecords()).willReturn(Failed)

        assertEquals(
            DeleteAllDataResultDomainModel.Failed,
            classUnderTest.executeInBackground(Unit)
        )
        verify(deletePreferencesRepository, never()).deletePreferences()
        verify(deleteFilesRepository, never()).deleteFiles()
    }

    @Test
    fun `Given a later step fails then failed, so the user can try again`() {
        given(deletePreferencesRepository.deletePreferences()).willReturn(Failed)
        assertEquals(
            DeleteAllDataResultDomainModel.Failed,
            classUnderTest.executeInBackground(Unit)
        )
        verify(deleteFilesRepository, never()).deleteFiles()

        given(deletePreferencesRepository.deletePreferences()).willReturn(Done)
        given(deleteFilesRepository.deleteFiles()).willReturn(Failed)
        assertEquals(
            DeleteAllDataResultDomainModel.Failed,
            classUnderTest.executeInBackground(Unit)
        )
    }

    private companion object {
        val Done = SettingsWriteResultDomainModel.Done
        val Failed = SettingsWriteResultDomainModel.Failed
    }
}
