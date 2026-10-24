package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLogDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationRequestDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.presentation.FIXTURE_TIME
import com.migrely.app.medication.presentation.FIXTURE_ZONE
import com.migrely.app.medication.presentation.IBUPROFEN
import com.migrely.app.medication.presentation.IBUPROFEN_DOMAIN
import com.migrely.app.medication.presentation.INTAKE
import com.migrely.app.medication.presentation.INTAKE_DOMAIN
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationLogPresentationModel
import com.migrely.app.medication.presentation.model.SaveProblemPresentationModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationMappersTest {
    @Test
    fun `When mapping a medication then every field`() {
        assertEquals(IBUPROFEN, MedicationPresentationMapper().toPresentation(IBUPROFEN_DOMAIN))
    }

    @Test
    fun `When mapping every kind and category then each has its counterpart`() {
        val mapper = MedicationPresentationMapper()
        val pairs = listOf(
            MedicationCategoryDomainModel.Triptan to MedicationCategoryPresentationModel.Triptan,
            MedicationCategoryDomainModel.CombinationOrOther to
                MedicationCategoryPresentationModel.CombinationOrOther,
            null to null
        )
        pairs.forEach { (domain, presentation) ->
            assertEquals(
                presentation,
                mapper.toPresentation(IBUPROFEN_DOMAIN.copy(category = domain)).category
            )
        }
        assertEquals(
            MedicationKindPresentationModel.Preventive,
            mapper.toPresentation(
                IBUPROFEN_DOMAIN.copy(kind = MedicationKindDomainModel.Preventive)
            ).kind
        )
    }

    @Test
    fun `When building a save request then the entered values`() {
        assertEquals(
            SaveMedicationRequestDomainModel(
                "m",
                "Sumatriptan",
                MedicationKindDomainModel.Acute,
                MedicationCategoryDomainModel.Triptan,
                "50 mg"
            ),
            MedicationDomainMapper().toDomain(
                "m",
                "Sumatriptan",
                MedicationKindPresentationModel.Acute,
                MedicationCategoryPresentationModel.Triptan,
                "50 mg"
            )
        )
        assertEquals(
            SaveMedicationRequestDomainModel(
                null,
                "Topiramate",
                MedicationKindDomainModel.Preventive,
                null,
                null
            ),
            MedicationDomainMapper().toDomain(
                null,
                "Topiramate",
                MedicationKindPresentationModel.Preventive,
                null,
                null
            )
        )
    }

    @Test
    fun `When mapping save results then only refusals are problems`() {
        val mapper = SaveProblemPresentationMapper()

        assertEquals(
            SaveProblemPresentationModel.Duplicate("Ibuprofen"),
            mapper.toPresentation(SaveMedicationResultDomainModel.Duplicate("Ibuprofen"))
        )
        assertEquals(
            SaveProblemPresentationModel.ArchivedName("Sumatriptan"),
            mapper.toPresentation(SaveMedicationResultDomainModel.ArchivedName("Sumatriptan"))
        )
        assertEquals(
            SaveProblemPresentationModel.LimitReached,
            mapper.toPresentation(SaveMedicationResultDomainModel.LimitReached)
        )
        assertEquals(
            SaveProblemPresentationModel.InvalidName,
            mapper.toPresentation(SaveMedicationResultDomainModel.InvalidName)
        )
        assertEquals(
            SaveProblemPresentationModel.InvalidDose,
            mapper.toPresentation(SaveMedicationResultDomainModel.InvalidDose)
        )
        listOf(
            SaveMedicationResultDomainModel.Saved(IBUPROFEN_DOMAIN),
            SaveMedicationResultDomainModel.Restored(IBUPROFEN_DOMAIN),
            SaveMedicationResultDomainModel.StorageFull,
            SaveMedicationResultDomainModel.Failed
        ).forEach { assertEquals(null, mapper.toPresentation(it)) }
    }

    @Test
    fun `When mapping a log then its time, zone, attack and medications`() {
        assertEquals(
            MedicationLogPresentationModel(FIXTURE_TIME, FIXTURE_ZONE, "a", listOf(IBUPROFEN)),
            MedicationLogPresentationMapper(MedicationPresentationMapper()).toPresentation(
                MedicationLogDomainModel(FIXTURE_TIME, FIXTURE_ZONE, "a", listOf(IBUPROFEN_DOMAIN))
            )
        )
    }

    @Test
    fun `When mapping an intake then every field`() {
        assertEquals(INTAKE, IntakePresentationMapper().toPresentation(INTAKE_DOMAIN))
    }
}
