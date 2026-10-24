package com.migrely.app.datasource.attack.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class AttackLocalMapperTest {
    private val classUnderTest = AttackLocalMapper()

    @Test
    fun `Given an ended attack with unordered points then maps fields and orders points`() {
        // Given
        val local = AttackWithPointsLocalModel(
            attack = AttackLocalModel(
                id = "a",
                startEpochMillis = 1_000L,
                startZoneId = "Asia/Tashkent",
                endEpochMillis = 9_000L,
                longCheckConfirmedEpochMillis = 5_000L,
                longCheckDisabled = true,
                createdEpochMillis = 1_000L,
                updatedEpochMillis = 9_000L
            ),
            intensityPoints = listOf(
                IntensityPointLocalModel(id = 2, attackId = "a", atEpochMillis = 4_000L, value = 9),
                IntensityPointLocalModel(id = 1, attackId = "a", atEpochMillis = 2_000L, value = 6)
            )
        )
        val expected = AttackDataModel(
            id = "a",
            start = Instant.ofEpochMilli(1_000L),
            startZoneId = "Asia/Tashkent",
            end = Instant.ofEpochMilli(9_000L),
            intensityPoints = listOf(
                IntensityPointDataModel(Instant.ofEpochMilli(2_000L), 6),
                IntensityPointDataModel(Instant.ofEpochMilli(4_000L), 9)
            ),
            longCheckConfirmedAt = Instant.ofEpochMilli(5_000L),
            longCheckDisabled = true
        )

        // When
        val actual = classUnderTest.toData(local)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `Given an attack in progress without points then end is null`() {
        // Given
        val local = AttackWithPointsLocalModel(
            attack = AttackLocalModel("a", 1_000L, "UTC", null, null, false, 1_000L, 1_000L),
            intensityPoints = emptyList()
        )

        // When
        val actual = classUnderTest.toData(local)

        // Then
        assertEquals(
            AttackDataModel(
                "a",
                Instant.ofEpochMilli(1_000L),
                "UTC",
                null,
                emptyList(),
                null,
                false
            ),
            actual
        )
    }
}
