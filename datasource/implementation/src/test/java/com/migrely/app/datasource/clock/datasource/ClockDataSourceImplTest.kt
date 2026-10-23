package com.migrely.app.datasource.clock.datasource

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockDataSourceImplTest {
    @Test
    fun `When asked then returns the clock's instant and zone`() {
        // Given
        val instant = Instant.parse("2026-10-23T09:00:00Z")
        val zone = ZoneId.of("Asia/Tashkent")
        val classUnderTest = ClockDataSourceImpl { Clock.fixed(instant, zone) }

        // Then
        assertEquals(instant, classUnderTest.now())
        assertEquals(zone, classUnderTest.zone())
    }

    @Test
    fun `Given the device zone changes when asked again then returns the new zone`() {
        // Given
        var zone = ZoneId.of("Asia/Tashkent")
        val classUnderTest = ClockDataSourceImpl { Clock.fixed(Instant.EPOCH, zone) }

        // When
        zone = ZoneId.of("Europe/Berlin")

        // Then
        assertEquals(ZoneId.of("Europe/Berlin"), classUnderTest.zone())
    }
}
