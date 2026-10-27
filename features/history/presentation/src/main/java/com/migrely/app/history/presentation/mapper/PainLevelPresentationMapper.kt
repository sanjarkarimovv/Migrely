package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.PainLevelDomainModel
import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel

class PainLevelPresentationMapper {
    fun toPresentation(level: PainLevelDomainModel): IntensityLevelPresentationModel =
        when (level) {
            PainLevelDomainModel.Mild -> IntensityLevelPresentationModel.Mild
            PainLevelDomainModel.Moderate -> IntensityLevelPresentationModel.Moderate
            PainLevelDomainModel.Severe -> IntensityLevelPresentationModel.Severe
        }
}
