package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackIntakeEffectPresentationModel
import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel
import com.migrely.app.attack.ui.model.AttackIntakeEffectUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AttackIntakeUiMapperTest {
    @Test
    fun `When mapping an intake then every field, the time in millis and the zone id`() {
        val takenAt = Instant.parse("2026-10-24T05:30:00Z")

        assertEquals(
            AttackIntakeUiModel(
                "i",
                "m",
                "Ibuprofen",
                "400 mg",
                takenAt.toEpochMilli(),
                "Asia/Tokyo"
            ),
            AttackIntakeUiMapper().toUi(
                AttackIntakePresentationModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    "400 mg",
                    takenAt,
                    ZoneId.of("Asia/Tokyo")
                )
            )
        )
    }

    @Test
    fun `When mapping each answer there and back then the same answer`() {
        val takenAt = Instant.parse("2026-10-24T05:30:00Z")
        AttackIntakeEffectPresentationModel.entries.zip(AttackIntakeEffectUiModel.entries)
            .forEach { (presentation, ui) ->
                val intake = AttackIntakePresentationModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    null,
                    takenAt,
                    ZoneId.of("Asia/Tokyo"),
                    presentation
                )

                val mapped = AttackIntakeUiMapper().toUi(intake)

                assertEquals(ui, mapped.effect)
                assertEquals(intake, AttackIntakeUiToPresentationMapper().toPresentation(mapped))
            }
    }
}
