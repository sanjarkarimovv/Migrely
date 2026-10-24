package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.EffectQuestionPresentationModel
import com.migrely.app.medication.presentation.model.IntakeEffectPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.presentation.model.OveruseWarningPresentationModel
import com.migrely.app.medication.ui.mapper.OveruseCategoryUiMapper
import com.migrely.app.medication.ui.model.IntakeEffectUiModel
import com.migrely.app.medication.ui.model.MedicationPromptUiModel
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseLevelUiModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationPromptUiMappersTest {
    private val uiMapper = MedicationPromptUiMapper(OveruseCategoryUiMapper())
    private val presentationMapper = MedicationPromptUiToPresentationMapper()

    @Test
    fun `When mapping hidden then hidden`() {
        assertEquals(
            MedicationPromptUiModel.Hidden,
            uiMapper.toUi(MedicationPromptViewState.Hidden)
        )
    }

    @Test
    fun `When mapping a question then its time and zone as values`() {
        val takenAt = Instant.parse("2026-10-31T08:00:00Z")

        assertEquals(
            MedicationPromptUiModel.EffectQuestion(
                "i",
                "Ibuprofen",
                "400 mg",
                takenAt.toEpochMilli(),
                "Asia/Tokyo"
            ),
            uiMapper.toUi(
                MedicationPromptViewState.EffectQuestion(
                    EffectQuestionPresentationModel(
                        "i",
                        "Ibuprofen",
                        "400 mg",
                        takenAt,
                        ZoneId.of("Asia/Tokyo")
                    )
                )
            )
        )
    }

    @Test
    fun `When mapping each warning then its category and level`() {
        OveruseCategoryPresentationModel.entries.zip(OveruseCategoryUiModel.entries)
            .forEach { (category, expected) ->
                assertEquals(
                    MedicationPromptUiModel.OveruseWarning(
                        expected,
                        OveruseLevelUiModel.Doctor,
                        days = 10,
                        thresholdDays = 10
                    ),
                    uiMapper.toUi(
                        MedicationPromptViewState.OveruseWarning(
                            OveruseWarningPresentationModel(
                                category,
                                OveruseLevelPresentationModel.Doctor,
                                days = 10,
                                thresholdDays = 10
                            )
                        )
                    )
                )
            }
    }

    @Test
    fun `When mapping answers and warnings back then the same values`() {
        assertEquals(
            IntakeEffectPresentationModel.entries,
            IntakeEffectUiModel.entries.map(presentationMapper::toPresentation)
        )
        assertEquals(
            OveruseCategoryPresentationModel.entries,
            OveruseCategoryUiModel.entries.map(presentationMapper::toPresentation)
        )
        assertEquals(
            OveruseLevelPresentationModel.entries,
            OveruseLevelUiModel.entries.map(presentationMapper::toPresentation)
        )
    }
}
