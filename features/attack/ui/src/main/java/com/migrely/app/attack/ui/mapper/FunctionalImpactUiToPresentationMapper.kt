package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel

class FunctionalImpactUiToPresentationMapper {
    fun toPresentation(
        functionalImpact: FunctionalImpactUiModel
    ): FunctionalImpactPresentationModel = when (
        functionalImpact
    ) {
        FunctionalImpactUiModel.None -> FunctionalImpactPresentationModel.None
        FunctionalImpactUiModel.LessActive -> FunctionalImpactPresentationModel.LessActive
        FunctionalImpactUiModel.HadToStop -> FunctionalImpactPresentationModel.HadToStop
        FunctionalImpactUiModel.BedRest -> FunctionalImpactPresentationModel.BedRest
    }
}
