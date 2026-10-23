package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel
import com.migrely.app.attack.ui.model.HeadLocationUiModel

class HeadLocationUiToPresentationMapper {
    fun toPresentation(headLocation: HeadLocationUiModel): HeadLocationPresentationModel = when (
        headLocation
    ) {
        HeadLocationUiModel.Left -> HeadLocationPresentationModel.Left
        HeadLocationUiModel.Right -> HeadLocationPresentationModel.Right
        HeadLocationUiModel.BothSides -> HeadLocationPresentationModel.BothSides
        HeadLocationUiModel.Forehead -> HeadLocationPresentationModel.Forehead
        HeadLocationUiModel.BackOfHead -> HeadLocationPresentationModel.BackOfHead
        HeadLocationUiModel.AroundEyes -> HeadLocationPresentationModel.AroundEyes
        HeadLocationUiModel.WholeHead -> HeadLocationPresentationModel.WholeHead
    }
}
