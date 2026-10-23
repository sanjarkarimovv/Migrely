package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel

class FunctionalImpactDomainMapper {
    fun toDomain(functionalImpact: FunctionalImpactPresentationModel): FunctionalImpactDomainModel =
        when (
            functionalImpact
        ) {
            FunctionalImpactPresentationModel.None -> FunctionalImpactDomainModel.None
            FunctionalImpactPresentationModel.LessActive -> FunctionalImpactDomainModel.LessActive
            FunctionalImpactPresentationModel.HadToStop -> FunctionalImpactDomainModel.HadToStop
            FunctionalImpactPresentationModel.BedRest -> FunctionalImpactDomainModel.BedRest
        }
}
