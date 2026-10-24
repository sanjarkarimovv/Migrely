package com.migrely.app.home.presentation.mapper

import com.migrely.app.home.domain.model.IntensityLevelDomainModel
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import org.junit.Assert.assertEquals
import org.junit.Test

class IntensityLevelPresentationMapperTest {
    private val classUnderTest = IntensityLevelPresentationMapper()

    @Test
    fun `When mapping each pain value then uses the 3-6-10 boundaries`() {
        val expected = (1..10).associateWith {
            when (it) {
                in 1..3 -> IntensityLevelPresentationModel.Mild
                in 4..6 -> IntensityLevelPresentationModel.Moderate
                else -> IntensityLevelPresentationModel.Severe
            }
        }

        assertEquals(expected, (1..10).associateWith(classUnderTest::toPresentation))
    }

    @Test
    fun `When mapping levels to domain then values are 3, 6 and 9`() {
        val classUnderTest = IntensityLevelDomainMapper()

        assertEquals(
            IntensityLevelDomainModel.Mild,
            classUnderTest.toDomain(IntensityLevelPresentationModel.Mild)
        )
        assertEquals(
            IntensityLevelDomainModel.Moderate,
            classUnderTest.toDomain(IntensityLevelPresentationModel.Moderate)
        )
        assertEquals(
            IntensityLevelDomainModel.Severe,
            classUnderTest.toDomain(IntensityLevelPresentationModel.Severe)
        )
    }
}
