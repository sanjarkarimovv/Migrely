package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-24T05:00:00Z")

class LinkedAttackDomainMapperTest {
    private val classUnderTest = LinkedAttackDomainMapper()

    private fun attack(zoneId: String) =
        AttackDataModel("a", START, zoneId, null, emptyList(), null, false)

    @Test
    fun `When mapping an attack then its times and the zone it started in`() {
        assertEquals(
            LinkedAttackDomainModel("a", START, null, ZoneId.of("Asia/Tokyo")),
            classUnderTest.toDomain(attack("Asia/Tokyo"))
        )
    }

    @Test
    fun `Given a zone this device does not know when mapping then UTC`() {
        assertEquals(ZoneOffset.UTC, classUnderTest.toDomain(attack("Mars/Olympus")).zone)
    }
}
