package com.migrely.app.ui.theme.mapper

import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.ui.theme.model.AppThemeUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AppThemeUiMapperTest(private val domain: ThemeDomainModel, private val ui: AppThemeUiModel) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} is drawn {1}")
        fun parameters() = listOf(
            arrayOf(ThemeDomainModel.Dark, AppThemeUiModel.Dark),
            arrayOf(ThemeDomainModel.Light, AppThemeUiModel.Light),
            arrayOf(ThemeDomainModel.System, AppThemeUiModel.System)
        )
    }

    @Test
    fun `When mapping then the same theme`() {
        assertEquals(ui, AppThemeUiMapper().toUi(domain))
    }
}
