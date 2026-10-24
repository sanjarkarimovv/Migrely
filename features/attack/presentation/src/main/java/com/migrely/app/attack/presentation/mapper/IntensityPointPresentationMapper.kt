package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel

class IntensityPointPresentationMapper {
    fun toPresentation(point: IntensityPointDomainModel) = IntensityPointPresentationModel(
        point.at,
        point.value
    )
}
