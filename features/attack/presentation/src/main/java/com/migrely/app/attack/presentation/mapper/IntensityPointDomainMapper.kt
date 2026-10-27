package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel

class IntensityPointDomainMapper {
    fun toDomain(point: IntensityPointPresentationModel) = IntensityPointDomainModel(
        point.at,
        point.value
    )
}
