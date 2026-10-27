package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackRangePresentationModel
import com.migrely.app.attack.presentation.model.PastAttackDraftPresentationModel
import com.migrely.app.attack.ui.model.AttackRangeUiModel
import com.migrely.app.attack.ui.model.PastAttackDraftUiModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class RangeAndDraftUiMapperTest {
    @Test
    fun `A range in progress keeps an empty end`() {
        assertEquals(
            AttackRangeUiModel(1_000L, null),
            AttackRangeUiMapper().toUi(
                AttackRangePresentationModel(Instant.ofEpochMilli(1_000L), null)
            )
        )
    }

    @Test
    fun `A draft keeps its times and zone id`() {
        assertEquals(
            PastAttackDraftUiModel(1_000L, 2_000L, "Asia/Tashkent"),
            PastAttackDraftUiMapper().toUi(
                PastAttackDraftPresentationModel(
                    Instant.ofEpochMilli(1_000L),
                    Instant.ofEpochMilli(2_000L),
                    ZoneId.of("Asia/Tashkent")
                )
            )
        )
    }
}
