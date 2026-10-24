package com.migrely.app.history.ui.mapper

import com.migrely.app.designsystem.sky.DaySkyAppearance
import com.migrely.app.history.ui.model.DaySkyUiModel

class DaySkyAppearanceUiMapper {
    fun toAppearance(sky: DaySkyUiModel): DaySkyAppearance = when (sky) {
        DaySkyUiModel.NothingLogged -> DaySkyAppearance.NothingLogged
        DaySkyUiModel.Future -> DaySkyAppearance.Future
        DaySkyUiModel.Hazy -> DaySkyAppearance.Hazy
        DaySkyUiModel.Cloudy -> DaySkyAppearance.Cloudy
        DaySkyUiModel.Storm -> DaySkyAppearance.Storm
        DaySkyUiModel.Rain -> DaySkyAppearance.Rain
    }
}
