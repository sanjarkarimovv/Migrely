package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel

class HeadLocationDomainMapper {
    fun toDomain(location: HeadLocationDataModel): HeadLocationDomainModel = when (location) {
        HeadLocationDataModel.Left -> HeadLocationDomainModel.Left
        HeadLocationDataModel.Right -> HeadLocationDomainModel.Right
        HeadLocationDataModel.BothSides -> HeadLocationDomainModel.BothSides
        HeadLocationDataModel.Forehead -> HeadLocationDomainModel.Forehead
        HeadLocationDataModel.BackOfHead -> HeadLocationDomainModel.BackOfHead
        HeadLocationDataModel.AroundEyes -> HeadLocationDomainModel.AroundEyes
        HeadLocationDataModel.WholeHead -> HeadLocationDomainModel.WholeHead
    }
}
