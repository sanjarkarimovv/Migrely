package com.migrely.app.home.ui.mapper

import com.migrely.app.designsystem.sky.SkyAppearance
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SmallUiMappersTest {
    @Test
    fun `When mapping each sky then uses the matching appearance`() {
        val classUnderTest = SkyAppearanceUiMapper()

        assertEquals(
            listOf(
                SkyAppearance.Calm,
                SkyAppearance.Clear,
                SkyAppearance.Hazy,
                SkyAppearance.Cloudy,
                SkyAppearance.Storm
            ),
            SkyUiModel.entries.map(classUnderTest::toAppearance)
        )
    }

    @Test
    fun `When mapping each intensity level then uses the matching presentation level`() {
        val classUnderTest = IntensityLevelUiToPresentationMapper()

        assertEquals(
            listOf(
                IntensityLevelPresentationModel.Mild,
                IntensityLevelPresentationModel.Moderate,
                IntensityLevelPresentationModel.Severe
            ),
            IntensityLevelUiModel.entries.map(classUnderTest::toPresentation)
        )
    }
}
