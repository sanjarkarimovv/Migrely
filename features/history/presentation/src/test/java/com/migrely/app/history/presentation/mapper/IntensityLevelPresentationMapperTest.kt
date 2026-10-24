package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class IntensityLevelPresentationMapperTest(
    private val value: Int,
    private val expected: IntensityLevelPresentationModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(1, IntensityLevelPresentationModel.Mild),
            arrayOf(3, IntensityLevelPresentationModel.Mild),
            arrayOf(4, IntensityLevelPresentationModel.Moderate),
            arrayOf(6, IntensityLevelPresentationModel.Moderate),
            arrayOf(7, IntensityLevelPresentationModel.Severe),
            arrayOf(10, IntensityLevelPresentationModel.Severe)
        )
    }

    @Test
    fun `When mapping then the level of the value`() {
        assertEquals(
            expected,
            IntensityLevelPresentationMapper(PainLevelPresentationMapper()).toPresentation(value)
        )
    }
}
