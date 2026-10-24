package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel

class HeadLocationDataMapper {
    fun toData(location: HeadLocationDomainModel): HeadLocationDataModel = when (location) {
        HeadLocationDomainModel.Left -> HeadLocationDataModel.Left
        HeadLocationDomainModel.Right -> HeadLocationDataModel.Right
        HeadLocationDomainModel.BothSides -> HeadLocationDataModel.BothSides
        HeadLocationDomainModel.Forehead -> HeadLocationDataModel.Forehead
        HeadLocationDomainModel.BackOfHead -> HeadLocationDataModel.BackOfHead
        HeadLocationDomainModel.AroundEyes -> HeadLocationDataModel.AroundEyes
        HeadLocationDomainModel.WholeHead -> HeadLocationDataModel.WholeHead
    }
}
