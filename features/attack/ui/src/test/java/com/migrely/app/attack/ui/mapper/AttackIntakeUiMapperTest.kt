package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel
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
}
