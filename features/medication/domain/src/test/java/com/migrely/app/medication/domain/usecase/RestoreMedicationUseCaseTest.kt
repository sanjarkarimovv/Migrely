package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.RestoreMedicationResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class RestoreMedicationUseCaseTest {
    private val countActiveMedicationsRepository: CountActiveMedicationsRepository = mock()
    private val setMedicationArchivedRepository: SetMedicationArchivedRepository = mock()
    private lateinit var classUnderTest: RestoreMedicationUseCase

    @Before
    fun setUp() {
        classUnderTest = RestoreMedicationUseCase(
            countActiveMedicationsRepository,
            setMedicationArchivedRepository,
            CoroutineContextProvider.Default
        )
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Counted(49))
        given(setMedicationArchivedRepository.setMedicationArchived("m", false))
            .willReturn(WriteResultDomainModel.Done)
    }

    @Test
    fun `Given 49 active when undoing an archive then restored`() {
        assertEquals(
            RestoreMedicationResultDomainModel.Restored,
            classUnderTest.executeInBackground("m")
        )
    }

    @Test
    fun `Given 50 active again when undoing an archive then limit reached and kept archived`() {
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Counted(50))

        assertEquals(
            RestoreMedicationResultDomainModel.LimitReached,
            classUnderTest.executeInBackground("m")
        )
        verify(setMedicationArchivedRepository, never()).setMedicationArchived(any(), any())
    }

    @Test
    fun `Given the write or the count fails when undoing an archive then that failure`() {
        given(setMedicationArchivedRepository.setMedicationArchived("m", false))
            .willReturn(WriteResultDomainModel.StorageFull)
        assertEquals(
            RestoreMedicationResultDomainModel.StorageFull,
            classUnderTest.executeInBackground("m")
        )

        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Failed)
        assertEquals(
            RestoreMedicationResultDomainModel.Failed,
            classUnderTest.executeInBackground("m")
        )
    }
}
