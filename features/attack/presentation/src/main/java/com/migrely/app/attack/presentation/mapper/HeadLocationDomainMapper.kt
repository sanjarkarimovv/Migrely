package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel

class HeadLocationDomainMapper {
    fun toDomain(headLocation: HeadLocationPresentationModel): HeadLocationDomainModel = when (
        headLocation
    ) {
        HeadLocationPresentationModel.Left -> HeadLocationDomainModel.Left
        HeadLocationPresentationModel.Right -> HeadLocationDomainModel.Right
        HeadLocationPresentationModel.BothSides -> HeadLocationDomainModel.BothSides
        HeadLocationPresentationModel.Forehead -> HeadLocationDomainModel.Forehead
        HeadLocationPresentationModel.BackOfHead -> HeadLocationDomainModel.BackOfHead
        HeadLocationPresentationModel.AroundEyes -> HeadLocationDomainModel.AroundEyes
        HeadLocationPresentationModel.WholeHead -> HeadLocationDomainModel.WholeHead
    }
}
