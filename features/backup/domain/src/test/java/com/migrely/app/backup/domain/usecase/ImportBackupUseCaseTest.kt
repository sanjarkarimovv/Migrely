package com.migrely.app.backup.domain.usecase

import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.repository.AddBackupRepository
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.ReplaceWithBackupRepository
import com.migrely.app.backup.domain.repository.SaveAutomaticBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private const val URI = "content://downloads/1"
private val REPLACED = ImportResultDomainModel.Replaced(BackupSummaryDomainModel(10, 2, 5))

class ImportBackupUseCaseTest {
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository = mock()
    private val saveAutomaticBackupRepository: SaveAutomaticBackupRepository = mock()
    private val replaceWithBackupRepository: ReplaceWithBackupRepository = mock()
    private val addBackupRepository: AddBackupRepository = mock()
    private val classUnderTest = ImportBackupUseCase(
        getPhoneRecordsRepository,
        saveAutomaticBackupRepository,
        replaceWithBackupRepository,
        addBackupRepository,
        CoroutineContextProvider.Default
    )

    private fun replace() = classUnderTest.executeInBackground(
        ImportBackupRequestDomainModel(URI, ImportModeDomainModel.Replace)
    )

    @Test
    fun `Given records on the phone when replacing then they are saved first, then replaced`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Some)
        given(
            saveAutomaticBackupRepository.saveAutomaticBackup()
        ).willReturn(BackupWriteResultDomainModel.Saved)
        given(replaceWithBackupRepository.replaceWithBackup(URI)).willReturn(REPLACED)

        assertEquals(REPLACED, replace())

        inOrder(saveAutomaticBackupRepository, replaceWithBackupRepository) {
            verify(saveAutomaticBackupRepository).saveAutomaticBackup()
            verify(replaceWithBackupRepository).replaceWithBackup(URI)
        }
    }

    @Test
    fun `Given an empty phone when replacing then there is nothing to save first`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.None)
        given(replaceWithBackupRepository.replaceWithBackup(URI)).willReturn(REPLACED)

        assertEquals(REPLACED, replace())
        verify(saveAutomaticBackupRepository, never()).saveAutomaticBackup()
    }

    @Test
    fun `Given the automatic backup cannot be saved when replacing then nothing is replaced`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Some)

        given(
            saveAutomaticBackupRepository.saveAutomaticBackup()
        ).willReturn(BackupWriteResultDomainModel.StorageFull)
        assertEquals(ImportResultDomainModel.StorageFull, replace())

        given(
            saveAutomaticBackupRepository.saveAutomaticBackup()
        ).willReturn(BackupWriteResultDomainModel.Failed)
        assertEquals(ImportResultDomainModel.Failed, replace())

        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Failed)
        assertEquals(ImportResultDomainModel.Failed, replace())

        verify(replaceWithBackupRepository, never()).replaceWithBackup(any())
    }

    @Test
    fun `When adding then the backup is added with no automatic backup`() {
        val added = ImportResultDomainModel.Added(addedAttacks = 6, skippedAttacks = 4)
        given(addBackupRepository.addBackup(URI)).willReturn(added)

        assertEquals(
            added,
            classUnderTest.executeInBackground(
                ImportBackupRequestDomainModel(URI, ImportModeDomainModel.Add)
            )
        )
        verify(saveAutomaticBackupRepository, never()).saveAutomaticBackup()
    }
}
