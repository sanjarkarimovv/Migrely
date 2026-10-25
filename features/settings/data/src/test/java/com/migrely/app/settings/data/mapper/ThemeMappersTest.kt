package com.migrely.app.settings.data.mapper

import com.migrely.app.datasource.preferences.model.ThemeDataModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ThemeMappersTest(private val data: ThemeDataModel, private val domain: ThemeDomainModel) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} is {1}")
        fun parameters() = listOf(
            arrayOf(ThemeDataModel.Dark, ThemeDomainModel.Dark),
            arrayOf(ThemeDataModel.Light, ThemeDomainModel.Light),
            arrayOf(ThemeDataModel.System, ThemeDomainModel.System)
        )
    }

    @Test
    fun `When mapping either way then the same theme`() {
        assertEquals(domain, ThemeDomainMapper().toDomain(data))
        assertEquals(data, ThemeDataMapper().toData(domain))
    }
}
