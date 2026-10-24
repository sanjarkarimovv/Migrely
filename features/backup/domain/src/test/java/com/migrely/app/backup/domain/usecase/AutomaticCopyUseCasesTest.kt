package com.migrely.app.backup.domain.usecase

import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import com.migrely.app.backup.domain.repository.GetAutomaticCopyRepository
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.RestoreAutomaticCopyRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val SUMMARY = BackupSummaryDomainModel(attacks = 16, medications = 2, dayMarks = 5)
private val RESTORED = RestoreResultDomainModel.Restored(SUMMARY)
private val MADE_AT = Instant.parse("2026-10-24T19:30:00Z")

class AutomaticCopyUseCasesTest {
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository = mock()
    private val restoreAutomaticCopyRepository: RestoreAutomaticCopyRepository = mock()
    private val restore = RestoreAutomaticCopyUseCase(
        getPhoneRecordsRepository,
        restoreAutomaticCopyRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `When asked for the copy then when it was made and what it holds, or none`() {
        val repository: GetAutomaticCopyRepository = mock()
        val copy = AutomaticCopyDomainModel.Available(MADE_AT, SUMMARY)
        val classUnderTest = GetAutomaticCopyUseCase(repository, CoroutineContextProvider.Default)

        given(repository.automaticCopy()).willReturn(copy)
        assertEquals(copy, classUnderTest.executeInBackground(Unit))

        given(repository.automaticCopy()).willReturn(AutomaticCopyDomainModel.None)
        assertEquals(AutomaticCopyDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `Given records on the phone when restoring then they are saved as a copy first`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Some)
        given(
            restoreAutomaticCopyRepository.restoreAutomaticCopy(MADE_AT, saveCopyFirst = true)
        ).willReturn(RESTORED)

        assertEquals(RESTORED, restore.executeInBackground(MADE_AT))
    }

    @Test
    fun `Given an empty phone when restoring then no copy pushes an older one out`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.None)
        given(
            restoreAutomaticCopyRepository.restoreAutomaticCopy(MADE_AT, saveCopyFirst = false)
        ).willReturn(RESTORED)

        assertEquals(RESTORED, restore.executeInBackground(MADE_AT))
    }

    @Test
    fun `Given the phone cannot be read when restoring then nothing is replaced`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Failed)

        assertEquals(RestoreResultDomainModel.Failed, restore.executeInBackground(MADE_AT))
        verify(restoreAutomaticCopyRepository, never()).restoreAutomaticCopy(any(), any())
    }
}
