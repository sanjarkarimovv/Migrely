package com.migrely.app.home.presentation.mapper

import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel

/** Maps a 1–10 pain value to the three levels shown during an attack. */
class IntensityLevelPresentationMapper {
    fun toPresentation(value: Int): IntensityLevelPresentationModel = when (value) {
        in MILD -> IntensityLevelPresentationModel.Mild
        in MODERATE -> IntensityLevelPresentationModel.Moderate
        else -> IntensityLevelPresentationModel.Severe
    }

    private companion object {
        val MILD = 1..3
        val MODERATE = 4..6
    }
}
