package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel

class FunctionalImpactPresentationMapper {
    fun toPresentation(
        functionalImpact: FunctionalImpactDomainModel
    ): FunctionalImpactPresentationModel = when (
        functionalImpact
    ) {
        FunctionalImpactDomainModel.None -> FunctionalImpactPresentationModel.None
        FunctionalImpactDomainModel.LessActive -> FunctionalImpactPresentationModel.LessActive
        FunctionalImpactDomainModel.HadToStop -> FunctionalImpactPresentationModel.HadToStop
        FunctionalImpactDomainModel.BedRest -> FunctionalImpactPresentationModel.BedRest
    }
}
