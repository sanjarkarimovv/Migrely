package com.migrely.app.ui.theme.mapper

import com.migrely.app.settings.domain.model.AppearanceDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.ui.theme.model.AppThemeUiModel
import com.migrely.app.ui.theme.model.AppearanceUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class AppearanceUiMapperTest {
    @Test
    fun `When mapping then the theme and whether an attack is in progress`() {
        assertEquals(
            AppearanceUiModel(AppThemeUiModel.Light, attackInProgress = true),
            AppearanceUiMapper(AppThemeUiMapper()).toUi(
                AppearanceDomainModel(ThemeDomainModel.Light, attackInProgress = true)
            )
        )
    }
}

/** Dark while an attack is in progress in every theme, then the chosen theme again (BRD D94). */
@RunWith(Parameterized::class)
class DarkThemeUiResolverTest(
    private val theme: AppThemeUiModel,
    private val attackInProgress: Boolean,
    private val systemDark: Boolean,
    private val dark: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, attack {1}, phone dark {2}: dark {3}")
        fun parameters() = listOf(
            arrayOf<Any?>(AppThemeUiModel.Dark, false, false, true),
            arrayOf<Any?>(AppThemeUiModel.Light, false, true, false),
            arrayOf<Any?>(AppThemeUiModel.System, false, true, true),
            arrayOf<Any?>(AppThemeUiModel.System, false, false, false),
            arrayOf<Any?>(AppThemeUiModel.Light, true, false, true),
            arrayOf<Any?>(AppThemeUiModel.System, true, false, true),
            arrayOf<Any?>(AppThemeUiModel.Dark, true, false, true)
        )
    }

    @Test
    fun `When resolving then dark during an attack, otherwise as chosen`() {
        assertEquals(
            dark,
            DarkThemeUiResolver().resolve(AppearanceUiModel(theme, attackInProgress), systemDark)
        )
    }
}
