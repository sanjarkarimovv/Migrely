package com.migrely.app.home.presentation.mapper

import com.migrely.app.home.domain.model.IntensityLevelDomainModel
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel

class IntensityLevelDomainMapper {
    fun toDomain(level: IntensityLevelPresentationModel): IntensityLevelDomainModel = when (level) {
        IntensityLevelPresentationModel.Mild -> IntensityLevelDomainModel.Mild
        IntensityLevelPresentationModel.Moderate -> IntensityLevelDomainModel.Moderate
        IntensityLevelPresentationModel.Severe -> IntensityLevelDomainModel.Severe
    }
}
