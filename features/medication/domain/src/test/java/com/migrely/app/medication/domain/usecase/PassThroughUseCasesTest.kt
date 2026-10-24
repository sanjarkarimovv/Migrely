package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetMedicationRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class PassThroughUseCasesTest {
    private val context = CoroutineContextProvider.Default

    @Test
    fun `When getting medications then the repository's list`() {
        val repository: GetActiveMedicationsRepository = mock()
        given(repository.activeMedications()).willReturn(MedicationsResultDomainModel.Failed)

        assertEquals(
            MedicationsResultDomainModel.Failed,
            GetMedicationsUseCase(repository, context).executeInBackground(Unit)
        )
    }

    @Test
    fun `When getting one medication then the repository's lookup`() {
        val repository: GetMedicationRepository = mock()
        given(repository.medication("m")).willReturn(MedicationLookupResultDomainModel.NotFound)

        assertEquals(
            MedicationLookupResultDomainModel.NotFound,
            GetMedicationUseCase(repository, context).executeInBackground("m")
        )
    }

    @Test
    fun `When archiving and restoring then the archived flag is set and cleared`() {
        val repository: SetMedicationArchivedRepository = mock()
        given(repository.setMedicationArchived("m", true)).willReturn(WriteResultDomainModel.Done)
        given(
            repository.setMedicationArchived("m", false)
        ).willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(
            WriteResultDomainModel.Done,
            ArchiveMedicationUseCase(repository, context).executeInBackground("m")
        )
        assertEquals(
            WriteResultDomainModel.StorageFull,
            RestoreMedicationUseCase(repository, context).executeInBackground("m")
        )
    }

    @Test
    fun `When undoing an intake then it is deleted`() {
        val repository: DeleteIntakeRepository = mock()
        given(repository.deleteIntake("i")).willReturn(WriteResultDomainModel.Failed)

        assertEquals(
            WriteResultDomainModel.Failed,
            UndoIntakeUseCase(repository, context).executeInBackground("i")
        )
    }
}
