package com.migrely.app.attack.ui.mapper

import androidx.annotation.StringRes
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.BuiltInSymptomUiModel

class BuiltInSymptomLabelUiMapper {
    @StringRes
    fun toUi(builtInSymptom: BuiltInSymptomUiModel): Int = when (builtInSymptom) {
        BuiltInSymptomUiModel.Aura -> R.string.symptom_aura
        BuiltInSymptomUiModel.Nausea -> R.string.symptom_nausea
        BuiltInSymptomUiModel.Vomiting -> R.string.symptom_vomiting
        BuiltInSymptomUiModel.LightSensitivity -> R.string.symptom_light_sensitivity
        BuiltInSymptomUiModel.SoundSensitivity -> R.string.symptom_sound_sensitivity
        BuiltInSymptomUiModel.SmellSensitivity -> R.string.symptom_smell_sensitivity
        BuiltInSymptomUiModel.Dizziness -> R.string.symptom_dizziness
        BuiltInSymptomUiModel.NeckPain -> R.string.symptom_neck_pain
    }
}
