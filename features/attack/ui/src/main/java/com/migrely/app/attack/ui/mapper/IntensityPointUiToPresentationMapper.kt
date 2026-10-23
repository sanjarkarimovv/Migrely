package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import java.time.Instant

class IntensityPointUiToPresentationMapper {
    fun toPresentation(point: IntensityPointUiModel) =
        IntensityPointPresentationModel(Instant.ofEpochMilli(point.atEpochMillis), point.value)
}
