package com.migrely.app.attack.ui.mapper

import androidx.annotation.StringRes
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel

class FunctionalImpactLabelUiMapper {
    @StringRes
    fun toUi(functionalImpact: FunctionalImpactUiModel): Int = when (functionalImpact) {
        FunctionalImpactUiModel.None -> R.string.impact_none
        FunctionalImpactUiModel.LessActive -> R.string.impact_less_active
        FunctionalImpactUiModel.HadToStop -> R.string.impact_had_to_stop
        FunctionalImpactUiModel.BedRest -> R.string.impact_bed_rest
    }
}
