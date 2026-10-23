package com.migrely.app.attack.ui.view

import com.migrely.app.attack.ui.mapper.BuiltInSymptomLabelUiMapper
import com.migrely.app.attack.ui.mapper.BuiltInTriggerLabelUiMapper
import com.migrely.app.attack.ui.mapper.FunctionalImpactLabelUiMapper
import com.migrely.app.attack.ui.mapper.HeadLocationLabelUiMapper

/** The label mappers the details screen needs, grouped so its composables keep short signatures. */
data class AttackLabels(
    val location: HeadLocationLabelUiMapper,
    val symptom: BuiltInSymptomLabelUiMapper,
    val trigger: BuiltInTriggerLabelUiMapper,
    val impact: FunctionalImpactLabelUiMapper
) {
    companion object {
        val Default = AttackLabels(
            HeadLocationLabelUiMapper(),
            BuiltInSymptomLabelUiMapper(),
            BuiltInTriggerLabelUiMapper(),
            FunctionalImpactLabelUiMapper()
        )
    }
}
