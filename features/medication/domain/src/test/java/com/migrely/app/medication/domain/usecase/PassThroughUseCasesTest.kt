package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AcknowledgeOveruseRequestDomainModel
import com.migrely.app.medication.domain.model.AnswerEffectRequestDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.AcknowledgeOveruseRepository
import com.migrely.app.medication.domain.repository.CloseEffectQuestionRepository
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetMedicationRepository
import com.migrely.app.medication.domain.repository.SetIntakeEffectRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import java.time.Instant
import java.time.ZoneId
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
    fun `When archiving then the archived flag is set`() {
        val repository: SetMedicationArchivedRepository = mock()
        given(repository.setMedicationArchived("m", true)).willReturn(WriteResultDomainModel.Done)

        assertEquals(
            WriteResultDomainModel.Done,
            ArchiveMedicationUseCase(repository, context).executeInBackground("m")
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

    @Test
    fun `When answering then the answer is stored`() {
        val repository: SetIntakeEffectRepository = mock()
        given(repository.setIntakeEffect("i", IntakeEffectDomainModel.ALot))
            .willReturn(WriteResultDomainModel.Done)

        assertEquals(
            WriteResultDomainModel.Done,
            AnswerEffectUseCase(repository, context).executeInBackground(
                AnswerEffectRequestDomainModel("i", IntakeEffectDomainModel.ALot)
            )
        )
    }

    @Test
    fun `When skipping a question then it is closed`() {
        val repository: CloseEffectQuestionRepository = mock()
        given(repository.closeEffectQuestion("i")).willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(
            WriteResultDomainModel.StorageFull,
            SkipEffectQuestionUseCase(repository, context).executeInBackground("i")
        )
    }

    @Test
    fun `When acknowledging a warning then it is stored at the current time`() {
        val now = Instant.parse("2026-10-31T11:00:00Z")
        val timeRepository: GetCurrentTimeRepository = mock()
        given(timeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(now, ZoneId.of("Asia/Tashkent")))
        val repository: AcknowledgeOveruseRepository = mock()
        given(
            repository.acknowledgeOveruse(
                OveruseCategoryDomainModel.Triptan,
                OveruseLevelDomainModel.Doctor,
                now
            )
        ).willReturn(WriteResultDomainModel.Done)

        assertEquals(
            WriteResultDomainModel.Done,
            AcknowledgeOveruseUseCase(timeRepository, repository, context).executeInBackground(
                AcknowledgeOveruseRequestDomainModel(
                    OveruseCategoryDomainModel.Triptan,
                    OveruseLevelDomainModel.Doctor
                )
            )
        )
    }
}
