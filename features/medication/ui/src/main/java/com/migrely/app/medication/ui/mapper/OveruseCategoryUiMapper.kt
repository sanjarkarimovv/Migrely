package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel

class OveruseCategoryUiMapper {
    fun toUi(category: OveruseCategoryPresentationModel) = when (category) {
        OveruseCategoryPresentationModel.SimplePainkiller -> OveruseCategoryUiModel.SimplePainkiller
        OveruseCategoryPresentationModel.Triptan -> OveruseCategoryUiModel.Triptan
        OveruseCategoryPresentationModel.CombinationOrOther ->
            OveruseCategoryUiModel.CombinationOrOther
        OveruseCategoryPresentationModel.Multiple -> OveruseCategoryUiModel.Multiple
    }
}
