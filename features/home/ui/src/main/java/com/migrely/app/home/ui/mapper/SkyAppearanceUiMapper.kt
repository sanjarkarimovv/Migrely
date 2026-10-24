package com.migrely.app.home.ui.mapper

import com.migrely.app.designsystem.sky.SkyAppearance
import com.migrely.app.home.ui.model.SkyUiModel

class SkyAppearanceUiMapper {
    fun toAppearance(sky: SkyUiModel): SkyAppearance = when (sky) {
        SkyUiModel.Calm -> SkyAppearance.Calm
        SkyUiModel.Hazy -> SkyAppearance.Hazy
        SkyUiModel.Cloudy -> SkyAppearance.Cloudy
        SkyUiModel.Storm -> SkyAppearance.Storm
    }
}
