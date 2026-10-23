package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel

class FunctionalImpactUiMapper {
    fun toUi(functionalImpact: FunctionalImpactPresentationModel): FunctionalImpactUiModel = when (
        functionalImpact
    ) {
        FunctionalImpactPresentationModel.None -> FunctionalImpactUiModel.None
        FunctionalImpactPresentationModel.LessActive -> FunctionalImpactUiModel.LessActive
        FunctionalImpactPresentationModel.HadToStop -> FunctionalImpactUiModel.HadToStop
        FunctionalImpactPresentationModel.BedRest -> FunctionalImpactUiModel.BedRest
    }
}
