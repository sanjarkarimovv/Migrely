package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel

class HeadLocationPresentationMapper {
    fun toPresentation(headLocation: HeadLocationDomainModel): HeadLocationPresentationModel =
        when (
            headLocation
        ) {
            HeadLocationDomainModel.Left -> HeadLocationPresentationModel.Left
            HeadLocationDomainModel.Right -> HeadLocationPresentationModel.Right
            HeadLocationDomainModel.BothSides -> HeadLocationPresentationModel.BothSides
            HeadLocationDomainModel.Forehead -> HeadLocationPresentationModel.Forehead
            HeadLocationDomainModel.BackOfHead -> HeadLocationPresentationModel.BackOfHead
            HeadLocationDomainModel.AroundEyes -> HeadLocationPresentationModel.AroundEyes
            HeadLocationDomainModel.WholeHead -> HeadLocationPresentationModel.WholeHead
        }
}
