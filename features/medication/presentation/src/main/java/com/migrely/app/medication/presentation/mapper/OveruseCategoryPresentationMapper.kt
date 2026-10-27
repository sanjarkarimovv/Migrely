package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel

class OveruseCategoryPresentationMapper {
    fun toPresentation(category: OveruseCategoryDomainModel) = when (category) {
        OveruseCategoryDomainModel.SimplePainkiller ->
            OveruseCategoryPresentationModel.SimplePainkiller
        OveruseCategoryDomainModel.Triptan -> OveruseCategoryPresentationModel.Triptan
        OveruseCategoryDomainModel.CombinationOrOther ->
            OveruseCategoryPresentationModel.CombinationOrOther
        OveruseCategoryDomainModel.Multiple -> OveruseCategoryPresentationModel.Multiple
    }
}
