package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel
import com.migrely.app.attack.ui.model.HeadLocationUiModel

class HeadLocationUiMapper {
    fun toUi(headLocation: HeadLocationPresentationModel): HeadLocationUiModel = when (
        headLocation
    ) {
        HeadLocationPresentationModel.Left -> HeadLocationUiModel.Left
        HeadLocationPresentationModel.Right -> HeadLocationUiModel.Right
        HeadLocationPresentationModel.BothSides -> HeadLocationUiModel.BothSides
        HeadLocationPresentationModel.Forehead -> HeadLocationUiModel.Forehead
        HeadLocationPresentationModel.BackOfHead -> HeadLocationUiModel.BackOfHead
        HeadLocationPresentationModel.AroundEyes -> HeadLocationUiModel.AroundEyes
        HeadLocationPresentationModel.WholeHead -> HeadLocationUiModel.WholeHead
    }
}
