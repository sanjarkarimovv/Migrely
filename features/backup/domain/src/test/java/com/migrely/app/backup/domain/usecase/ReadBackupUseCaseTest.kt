package com.migrely.app.backup.domain.usecase

import com.migrely.app.backup.domain.model.BackupFileDomainModel
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.ReadBackupResultDomainModel
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.ReadBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private const val URI = "content://downloads/1"
private val SUMMARY = BackupSummaryDomainModel(attacks = 10, medications = 2, dayMarks = 5)

class ReadBackupUseCaseTest {
    private val readBackupRepository: ReadBackupRepository = mock()
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository = mock()
    private val classUnderTest = ReadBackupUseCase(
        readBackupRepository,
        getPhoneRecordsRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `Given a readable backup then what it holds, and whether the phone has records`() {
        given(
            readBackupRepository.readBackup(URI)
        ).willReturn(BackupFileDomainModel.Readable(SUMMARY))

        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Some)
        assertEquals(
            ReadBackupResultDomainModel.Readable(SUMMARY, true),
            classUnderTest.executeInBackground(URI)
        )

        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.None)
        assertEquals(
            ReadBackupResultDomainModel.Readable(SUMMARY, false),
            classUnderTest.executeInBackground(URI)
        )

        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Failed)
        assertEquals(ReadBackupResultDomainModel.Failed, classUnderTest.executeInBackground(URI))
    }

    @Test
    fun `Given a file that cannot be imported then why, without looking at the phone`() {
        mapOf(
            BackupFileDomainModel.NotABackup to ReadBackupResultDomainModel.NotABackup,
            BackupFileDomainModel.Newer to ReadBackupResultDomainModel.Newer,
            BackupFileDomainModel.Failed to ReadBackupResultDomainModel.Failed
        ).forEach { (file, expected) ->
            given(readBackupRepository.readBackup(URI)).willReturn(file)
            assertEquals(expected, classUnderTest.executeInBackground(URI))
        }
        verify(getPhoneRecordsRepository, never()).phoneRecords()
    }
}
