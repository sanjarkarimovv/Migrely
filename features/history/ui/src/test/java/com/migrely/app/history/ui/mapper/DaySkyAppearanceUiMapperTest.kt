package com.migrely.app.history.ui.mapper

import com.migrely.app.designsystem.sky.DaySkyAppearance
import com.migrely.app.history.ui.model.DaySkyUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DaySkyAppearanceUiMapperTest(
    private val sky: DaySkyUiModel,
    private val expected: DaySkyAppearance
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(DaySkyUiModel.NothingLogged, DaySkyAppearance.NothingLogged),
            arrayOf(DaySkyUiModel.MedicationOnly, DaySkyAppearance.MedicationOnly),
            arrayOf(DaySkyUiModel.Future, DaySkyAppearance.Future),
            arrayOf(DaySkyUiModel.Clear, DaySkyAppearance.Clear),
            arrayOf(DaySkyUiModel.Hazy, DaySkyAppearance.Hazy),
            arrayOf(DaySkyUiModel.Cloudy, DaySkyAppearance.Cloudy),
            arrayOf(DaySkyUiModel.Storm, DaySkyAppearance.Storm),
            arrayOf(DaySkyUiModel.Rain, DaySkyAppearance.Rain)
        )
    }

    private val classUnderTest = DaySkyAppearanceUiMapper()

    @Test
    fun `When mapping a day's sky then its appearance`() {
        assertEquals(expected, classUnderTest.toAppearance(sky))
    }
}
