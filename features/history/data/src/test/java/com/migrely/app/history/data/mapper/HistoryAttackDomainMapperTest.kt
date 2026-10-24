package com.migrely.app.history.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-14T05:00:00Z")
private val END: Instant = Instant.parse("2026-10-14T07:00:00Z")

class HistoryAttackDomainMapperTest {
    private val classUnderTest = HistoryAttackDomainMapper(ZoneDomainMapper())

    private fun data(points: List<Int>, zone: String = "Asia/Tashkent", end: Instant? = END) =
        AttackDataModel(
            id = "a",
            start = START,
            startZoneId = zone,
            end = end,
            intensityPoints = points.mapIndexed { index, value ->
                IntensityPointDataModel(START.plusSeconds(index * 60L), value)
            },
            longCheckConfirmedAt = null,
            longCheckDisabled = false
        )

    @Test
    fun `Given pain entries when mapping then the peak is the highest`() {
        assertEquals(
            HistoryAttackDomainModel("a", START, ZoneId.of("Asia/Tashkent"), END, 8),
            classUnderTest.toDomain(data(listOf(4, 8, 6)))
        )
    }

    @Test
    fun `Given no pain entry when mapping then no peak`() {
        assertEquals(null, classUnderTest.toDomain(data(emptyList())).peakIntensity)
    }

    @Test
    fun `Given an attack in progress when mapping then no end`() {
        assertEquals(null, classUnderTest.toDomain(data(listOf(5), end = null)).end)
    }

    @Test
    fun `Given an unknown zone when mapping then UTC`() {
        assertEquals(
            ZoneOffset.UTC,
            classUnderTest.toDomain(data(listOf(5), zone = "Mars/Olympus")).startZone
        )
    }
}
