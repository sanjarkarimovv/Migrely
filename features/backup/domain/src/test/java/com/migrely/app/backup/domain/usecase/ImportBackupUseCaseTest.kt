package com.migrely.app.backup.domain.usecase

import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.repository.AddBackupRepository
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.ReplaceWithBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private const val URI = "content://downloads/1"
private val REPLACED = ImportResultDomainModel.Replaced(BackupSummaryDomainModel(10, 2, 5))

class ImportBackupUseCaseTest {
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository = mock()
    private val replaceWithBackupRepository: ReplaceWithBackupRepository = mock()
    private val addBackupRepository: AddBackupRepository = mock()
    private val classUnderTest = ImportBackupUseCase(
        getPhoneRecordsRepository,
        replaceWithBackupRepository,
        addBackupRepository,
        CoroutineContextProvider.Default
    )

    private fun replace() = classUnderTest.executeInBackground(
        ImportBackupRequestDomainModel(URI, ImportModeDomainModel.Replace)
    )

    @Test
    fun `Given records on the phone when replacing then a copy is saved first`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Some)
        given(
            replaceWithBackupRepository.replaceWithBackup(URI, saveCopyFirst = true)
        ).willReturn(REPLACED)

        assertEquals(REPLACED, replace())
    }

    @Test
    fun `Given an empty phone when replacing then there is nothing to save first`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.None)
        given(
            replaceWithBackupRepository.replaceWithBackup(URI, saveCopyFirst = false)
        ).willReturn(REPLACED)

        assertEquals(REPLACED, replace())
    }

    @Test
    fun `Given the phone cannot be read when replacing then nothing is replaced`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Failed)

        assertEquals(ImportResultDomainModel.Failed, replace())
        verify(replaceWithBackupRepository, never()).replaceWithBackup(any(), any())
    }

    @Test
    fun `When adding then the backup is added with no copy and no look at the phone`() {
        val added = ImportResultDomainModel.Added(
            addedAttacks = 6,
            skippedAttacks = 4,
            addedOthers = 3
        )
        given(addBackupRepository.addBackup(URI)).willReturn(added)

        assertEquals(
            added,
            classUnderTest.executeInBackground(
                ImportBackupRequestDomainModel(URI, ImportModeDomainModel.Add)
            )
        )
        verify(replaceWithBackupRepository, never()).replaceWithBackup(any(), any())
        verify(getPhoneRecordsRepository, never()).phoneRecords()
    }
}
