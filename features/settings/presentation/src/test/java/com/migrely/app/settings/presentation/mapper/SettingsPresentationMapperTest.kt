package com.migrely.app.settings.presentation.mapper

import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.presentation.model.SettingsPresentationModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsPresentationMapperTest {
    @Test
    fun `When mapping then Track periods as it is`() {
        listOf(true, false).forEach {
            assertEquals(
                SettingsPresentationModel(periodTracking = it),
                SettingsPresentationMapper().toPresentation(
                    SettingsDomainModel(periodTracking = it)
                )
            )
        }
    }
}
