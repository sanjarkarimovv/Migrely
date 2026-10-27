package com.migrely.app.home.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttackDomainModelTest {
    @Test
    fun `Given no intensity points when peakIntensity then null`() {
        // Given
        val classUnderTest = attack()

        // When
        val actual = classUnderTest.peakIntensity

        // Then
        assertNull(actual)
    }

    @Test
    fun `Given several points when peakIntensity then highest value`() {
        // Given
        val classUnderTest = attack(
            intensityPoints = listOf(
                IntensityPointDomainModel(Instant.parse("2026-10-23T09:10:00Z"), 6),
                IntensityPointDomainModel(Instant.parse("2026-10-23T10:30:00Z"), 9),
                IntensityPointDomainModel(Instant.parse("2026-10-23T12:00:00Z"), 3)
            )
        )

        // When
        val actual = classUnderTest.peakIntensity

        // Then
        assertEquals(9, actual)
    }

    @Test
    fun `Given unordered points when latestIntensityPoint then the latest by time`() {
        // Given
        val latest = IntensityPointDomainModel(Instant.parse("2026-10-23T12:00:00Z"), 3)
        val classUnderTest = attack(
            intensityPoints = listOf(
                latest,
                IntensityPointDomainModel(Instant.parse("2026-10-23T09:10:00Z"), 6)
            )
        )

        // When
        val actual = classUnderTest.latestIntensityPoint

        // Then
        assertEquals(latest, actual)
    }
}
