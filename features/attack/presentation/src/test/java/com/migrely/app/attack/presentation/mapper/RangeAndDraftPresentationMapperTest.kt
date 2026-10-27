package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.presentation.model.AttackRangePresentationModel
import com.migrely.app.attack.presentation.model.PastAttackDraftPresentationModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val END: Instant = Instant.parse("2026-10-23T11:00:00Z")

class RangeAndDraftPresentationMapperTest {
    @Test
    fun `An ended range keeps both times`() {
        assertEquals(
            AttackRangePresentationModel(START, END),
            AttackRangePresentationMapper().toPresentation(AttackRangeDomainModel(START, END))
        )
    }

    @Test
    fun `A range in progress keeps an empty end`() {
        assertEquals(
            AttackRangePresentationModel(START, null),
            AttackRangePresentationMapper().toPresentation(AttackRangeDomainModel(START, null))
        )
    }

    @Test
    fun `A draft keeps its times and zone`() {
        val zone = ZoneId.of("Asia/Tashkent")

        assertEquals(
            PastAttackDraftPresentationModel(START, END, zone),
            PastAttackDraftPresentationMapper().toPresentation(
                PastAttackDraftDomainModel(START, END, zone)
            )
        )
    }
}
