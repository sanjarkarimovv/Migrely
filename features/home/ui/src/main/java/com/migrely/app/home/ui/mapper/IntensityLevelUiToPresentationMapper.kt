package com.migrely.app.home.ui.mapper

import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel

class IntensityLevelUiToPresentationMapper {
    fun toPresentation(level: IntensityLevelUiModel): IntensityLevelPresentationModel =
        when (level) {
            IntensityLevelUiModel.Mild -> IntensityLevelPresentationModel.Mild
            IntensityLevelUiModel.Moderate -> IntensityLevelPresentationModel.Moderate
            IntensityLevelUiModel.Severe -> IntensityLevelPresentationModel.Severe
        }
}
