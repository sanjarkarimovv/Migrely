package com.migrely.app.settings.presentation.mapper

import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.presentation.model.SettingsPresentationModel
import com.migrely.app.settings.presentation.model.ThemePresentationModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class SettingsPresentationMapperTest {
    @Test
    fun `When mapping then Track periods, the theme and the version as they are`() {
        listOf(true, false).forEach {
            assertEquals(
                SettingsPresentationModel(it, ThemePresentationModel.System, "1.2.0", true),
                SettingsPresentationMapper(ThemePresentationMapper()).toPresentation(
                    SettingsDomainModel(it, ThemeDomainModel.System, "1.2.0", false)
                )
            )
        }
    }

    @Test
    fun `Share and rate show only when no attack is known to be in progress`() {
        mapOf(false to true, true to false, null to false).forEach { (attack, shown) ->
            assertEquals(
                "attack in progress: $attack",
                shown,
                SettingsPresentationMapper(ThemePresentationMapper()).toPresentation(
                    SettingsDomainModel(true, ThemeDomainModel.Dark, "1.4.0", attack)
                ).showShareAndRate
            )
        }
    }
}

@RunWith(Parameterized::class)
class ThemeMappersTest(
    private val domain: ThemeDomainModel,
    private val presentation: ThemePresentationModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} is {1}")
        fun parameters() = listOf(
            arrayOf(ThemeDomainModel.Dark, ThemePresentationModel.Dark),
            arrayOf(ThemeDomainModel.Light, ThemePresentationModel.Light),
            arrayOf(ThemeDomainModel.System, ThemePresentationModel.System)
        )
    }

    @Test
    fun `When mapping either way then the same theme`() {
        assertEquals(presentation, ThemePresentationMapper().toPresentation(domain))
        assertEquals(domain, ThemeDomainMapper().toDomain(presentation))
    }
}
