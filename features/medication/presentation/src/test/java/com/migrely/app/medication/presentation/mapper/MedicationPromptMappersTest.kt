package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.MedicationPromptDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.presentation.model.EffectQuestionPresentationModel
import com.migrely.app.medication.presentation.model.IntakeEffectPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.presentation.model.OveruseWarningPresentationModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationPromptMappersTest {
    private val presentationMapper = MedicationPromptPresentationMapper()
    private val domainMapper = MedicationPromptDomainMapper()

    @Test
    fun `When mapping no card then hidden`() {
        assertEquals(
            MedicationPromptViewState.Hidden,
            presentationMapper.toPresentation(MedicationPromptDomainModel.None)
        )
    }

    @Test
    fun `When mapping a question then every field`() {
        val takenAt = Instant.parse("2026-10-31T08:00:00Z")
        val zone = ZoneId.of("Asia/Tokyo")

        assertEquals(
            MedicationPromptViewState.EffectQuestion(
                EffectQuestionPresentationModel("i", "Ibuprofen", null, takenAt, zone)
            ),
            presentationMapper.toPresentation(
                MedicationPromptDomainModel.EffectQuestion(
                    PendingEffectQuestionDomainModel("i", "Ibuprofen", null, takenAt, zone)
                )
            )
        )
    }

    @Test
    fun `When mapping each warning then its category, level and threshold`() {
        listOf(
            OveruseCategoryDomainModel.SimplePainkiller to
                (OveruseCategoryPresentationModel.SimplePainkiller to 15),
            OveruseCategoryDomainModel.Triptan to (OveruseCategoryPresentationModel.Triptan to 10),
            OveruseCategoryDomainModel.CombinationOrOther to
                (OveruseCategoryPresentationModel.CombinationOrOther to 10),
            OveruseCategoryDomainModel.Multiple to (OveruseCategoryPresentationModel.Multiple to 10)
        ).forEach { (domain, expected) ->
            assertEquals(
                MedicationPromptViewState.OveruseWarning(
                    OveruseWarningPresentationModel(
                        expected.first,
                        OveruseLevelPresentationModel.Soft,
                        days = 8,
                        thresholdDays = expected.second
                    )
                ),
                presentationMapper.toPresentation(
                    MedicationPromptDomainModel.Overuse(domain, OveruseLevelDomainModel.Soft, 8)
                )
            )
        }
    }

    @Test
    fun `When mapping answers and warnings back then the same values`() {
        assertEquals(
            IntakeEffectDomainModel.entries,
            IntakeEffectPresentationModel.entries.map(domainMapper::toDomain)
        )
        assertEquals(
            OveruseCategoryDomainModel.entries,
            OveruseCategoryPresentationModel.entries.map(domainMapper::toDomain)
        )
        assertEquals(
            OveruseLevelDomainModel.entries,
            OveruseLevelPresentationModel.entries.map(domainMapper::toDomain)
        )
    }
}
