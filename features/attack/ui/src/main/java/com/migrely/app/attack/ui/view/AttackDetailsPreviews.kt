package com.migrely.app.attack.ui.view

import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.BuiltInSymptomUiModel
import com.migrely.app.attack.ui.model.BuiltInTriggerUiModel
import com.migrely.app.attack.ui.model.CustomTagUiModel
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel
import com.migrely.app.attack.ui.model.HeadLocationUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.SymptomUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TriggerUiModel

// 23 October 2026, 14:05 in Tashkent.
private const val PREVIEW_START = 1_792_746_300_000L
private const val HOUR = 3_600_000L

internal fun previewDetails(): AttackDetailsUiModel {
    val jawPain = SymptomUiModel.Custom(
        CustomTagUiModel("j", TagKindUiModel.Symptom, "Jaw pain", archived = false)
    )
    return AttackDetailsUiModel(
        id = "a",
        startEpochMillis = PREVIEW_START,
        startZoneId = "Asia/Tashkent",
        endEpochMillis = PREVIEW_START + 2 * HOUR + 25 * 60_000L,
        nowEpochMillis = PREVIEW_START + 5 * HOUR,
        intensityPoints = listOf(
            IntensityPointUiModel(PREVIEW_START + 10 * 60_000L, 6),
            IntensityPointUiModel(PREVIEW_START + HOUR, 9)
        ),
        peakIntensity = 9,
        selectedLocations = setOf(HeadLocationUiModel.Left, HeadLocationUiModel.AroundEyes),
        symptomChoices = BuiltInSymptomUiModel.entries.map(SymptomUiModel::BuiltIn) + jawPain,
        selectedSymptoms = setOf(SymptomUiModel.BuiltIn(BuiltInSymptomUiModel.Aura), jawPain),
        triggerChoices = BuiltInTriggerUiModel.entries.map(TriggerUiModel::BuiltIn),
        selectedTriggers = setOf(TriggerUiModel.BuiltIn(BuiltInTriggerUiModel.TooLittleSleep)),
        functionalImpact = FunctionalImpactUiModel.HadToStop,
        note = "Started after a long meeting with bright screens."
    )
}
