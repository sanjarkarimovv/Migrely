package com.migrely.app.home.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")

@RunWith(Parameterized::class)
class AttackDomainMapperTest(private val storedZoneId: String, private val expectedZone: ZoneId) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Given zone {0} then {1}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>("Asia/Tashkent", ZoneId.of("Asia/Tashkent")),
            arrayOf<Any>("Europe/Berlin", ZoneId.of("Europe/Berlin")),
            arrayOf<Any>("Mars/Olympus_Mons", ZoneOffset.UTC)
        )
    }

    private val classUnderTest = AttackDomainMapper(IntensityPointDomainMapper())

    @Test
    fun `When toDomain then maps every field`() {
        // Given
        val data = AttackDataModel(
            id = "a",
            start = START,
            startZoneId = storedZoneId,
            end = START.plusSeconds(3_600),
            intensityPoints = listOf(IntensityPointDataModel(START.plusSeconds(60), 6)),
            longCheckConfirmedAt = START.plusSeconds(120),
            longCheckDisabled = true
        )
        val expected = AttackDomainModel(
            id = "a",
            start = START,
            startZone = expectedZone,
            end = START.plusSeconds(3_600),
            intensityPoints = listOf(IntensityPointDomainModel(START.plusSeconds(60), 6)),
            longAttackCheck = LongAttackCheckDomainModel(
                confirmedAt = START.plusSeconds(120),
                disabled = true
            )
        )

        // When
        val actual = classUnderTest.toDomain(data)

        // Then
        assertEquals(expected, actual)
    }
}
