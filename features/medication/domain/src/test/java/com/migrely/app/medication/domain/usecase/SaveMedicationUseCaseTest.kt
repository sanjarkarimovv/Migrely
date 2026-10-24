package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationRequestDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.UpdateMedicationRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val ACUTE = MedicationKindDomainModel.Acute
private val SIMPLE = MedicationCategoryDomainModel.SimplePainkiller

private fun medication(id: String = "m", name: String = "Ibuprofen", archived: Boolean = false) =
    MedicationDomainModel(id, name, ACUTE, SIMPLE, "400 mg", archived)

class SaveMedicationUseCaseTest {
    private val findMedicationByNameRepository: FindMedicationByNameRepository = mock()
    private val countActiveMedicationsRepository: CountActiveMedicationsRepository = mock()
    private val createMedicationRepository: CreateMedicationRepository = mock()
    private val updateMedicationRepository: UpdateMedicationRepository = mock()
    private lateinit var classUnderTest: SaveMedicationUseCase

    @Before
    fun setUp() {
        classUnderTest = SaveMedicationUseCase(
            findMedicationByNameRepository,
            countActiveMedicationsRepository,
            createMedicationRepository,
            updateMedicationRepository,
            CoroutineContextProvider.Default
        )
        given(findMedicationByNameRepository.medicationByNameKey(any()))
            .willReturn(MedicationLookupResultDomainModel.NotFound)
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Counted(3))
        given(
            createMedicationRepository.createMedication(
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        )
            .willReturn(MedicationWriteResultDomainModel.Done(medication()))
        given(
            updateMedicationRepository.updateMedication(
                any(),
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).willReturn(MedicationWriteResultDomainModel.Done(medication()))
    }

    private fun request(
        name: String = " Ibuprofen ",
        kind: MedicationKindDomainModel = ACUTE,
        category: MedicationCategoryDomainModel? = SIMPLE,
        dose: String? = " 400 mg ",
        id: String? = null
    ) = SaveMedicationRequestDomainModel(id, name, kind, category, dose)

    @Test
    fun `Given a new name when saving then creates it trimmed with a lower-case key`() {
        val actual = classUnderTest.executeInBackground(request())

        assertEquals(SaveMedicationResultDomainModel.Saved(medication()), actual)
        verify(
            createMedicationRepository
        ).createMedication("Ibuprofen", "ibuprofen", ACUTE, SIMPLE, "400 mg")
    }

    @Test
    fun `Given a blank dose when saving then no dose`() {
        classUnderTest.executeInBackground(request(dose = "  "))

        verify(
            createMedicationRepository
        ).createMedication("Ibuprofen", "ibuprofen", ACUTE, SIMPLE, null)
    }

    @Test
    fun `Given a preventive medication when saving then its category is dropped`() {
        classUnderTest.executeInBackground(request(kind = MedicationKindDomainModel.Preventive))

        verify(createMedicationRepository).createMedication(
            "Ibuprofen",
            "ibuprofen",
            MedicationKindDomainModel.Preventive,
            null,
            "400 mg"
        )
    }

    @Test
    fun `Given a blank or 61-character name when saving then invalid`() {
        assertEquals(
            SaveMedicationResultDomainModel.InvalidName,
            classUnderTest.executeInBackground(request(name = "   "))
        )
        assertEquals(
            SaveMedicationResultDomainModel.InvalidName,
            classUnderTest.executeInBackground(request(name = "x".repeat(61)))
        )
    }

    @Test
    fun `Given a 60-character name when saving then saved`() {
        val actual = classUnderTest.executeInBackground(request(name = "x".repeat(60)))

        assertEquals(SaveMedicationResultDomainModel.Saved(medication()), actual)
    }

    @Test
    fun `Given a 21-character dose when saving then invalid`() {
        assertEquals(
            SaveMedicationResultDomainModel.InvalidDose,
            classUnderTest.executeInBackground(request(dose = "x".repeat(21)))
        )
    }

    @Test
    fun `Given an active medication with the name when adding then duplicate`() {
        given(findMedicationByNameRepository.medicationByNameKey("ibuprofen"))
            .willReturn(MedicationLookupResultDomainModel.Found(medication()))

        val actual = classUnderTest.executeInBackground(request(name = "IBUPROFEN"))

        assertEquals(SaveMedicationResultDomainModel.Duplicate("Ibuprofen"), actual)
        verify(createMedicationRepository, never())
            .createMedication(any(), any(), any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `Given an archived medication with the name when adding then it is restored`() {
        given(findMedicationByNameRepository.medicationByNameKey("ibuprofen"))
            .willReturn(
                MedicationLookupResultDomainModel.Found(medication(id = "old", archived = true))
            )

        val actual = classUnderTest.executeInBackground(request())

        assertEquals(SaveMedicationResultDomainModel.Restored(medication()), actual)
        verify(updateMedicationRepository)
            .updateMedication("old", "Ibuprofen", "ibuprofen", ACUTE, SIMPLE, "400 mg")
    }

    @Test
    fun `Given 50 active medications when adding then limit reached`() {
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Counted(50))

        assertEquals(
            SaveMedicationResultDomainModel.LimitReached,
            classUnderTest.executeInBackground(request())
        )
    }

    @Test
    fun `Given 50 active medications when restoring an archived one then limit reached`() {
        given(findMedicationByNameRepository.medicationByNameKey("ibuprofen"))
            .willReturn(
                MedicationLookupResultDomainModel.Found(medication(id = "old", archived = true))
            )
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Counted(50))

        assertEquals(
            SaveMedicationResultDomainModel.LimitReached,
            classUnderTest.executeInBackground(request())
        )
    }

    @Test
    fun `Given its own name when editing then updates it without a limit check`() {
        given(findMedicationByNameRepository.medicationByNameKey("ibuprofen"))
            .willReturn(MedicationLookupResultDomainModel.Found(medication(id = "m")))
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Counted(50))

        val actual = classUnderTest.executeInBackground(request(id = "m", dose = "200 mg"))

        assertEquals(SaveMedicationResultDomainModel.Saved(medication()), actual)
        verify(updateMedicationRepository)
            .updateMedication("m", "Ibuprofen", "ibuprofen", ACUTE, SIMPLE, "200 mg")
    }

    @Test
    fun `Given another medication's name when editing then duplicate, archived or not`() {
        given(findMedicationByNameRepository.medicationByNameKey("ibuprofen"))
            .willReturn(
                MedicationLookupResultDomainModel.Found(medication(id = "other", archived = true))
            )

        assertEquals(
            SaveMedicationResultDomainModel.Duplicate("Ibuprofen"),
            classUnderTest.executeInBackground(request(id = "m"))
        )
    }

    @Test
    fun `Given the lookup fails when saving then failed`() {
        given(findMedicationByNameRepository.medicationByNameKey(any()))
            .willReturn(MedicationLookupResultDomainModel.Failed)

        assertEquals(
            SaveMedicationResultDomainModel.Failed,
            classUnderTest.executeInBackground(request())
        )
    }

    @Test
    fun `Given the count fails when adding then failed`() {
        given(countActiveMedicationsRepository.activeMedicationCount())
            .willReturn(ActiveCountResultDomainModel.Failed)

        assertEquals(
            SaveMedicationResultDomainModel.Failed,
            classUnderTest.executeInBackground(request())
        )
    }

    @Test
    fun `Given the disk is full when adding then storage full`() {
        given(
            createMedicationRepository.createMedication(
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        )
            .willReturn(MedicationWriteResultDomainModel.StorageFull)

        assertEquals(
            SaveMedicationResultDomainModel.StorageFull,
            classUnderTest.executeInBackground(request())
        )
    }
}
