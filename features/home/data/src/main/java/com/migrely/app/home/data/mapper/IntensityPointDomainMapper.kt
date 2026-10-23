package com.migrely.app.home.data.mapper

import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel

class IntensityPointDomainMapper {
    fun toDomain(point: IntensityPointDataModel) =
        IntensityPointDomainModel(at = point.at, value = point.value)
}
