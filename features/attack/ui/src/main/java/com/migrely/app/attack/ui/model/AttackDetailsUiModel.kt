package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** What the details screen shows; [endEpochMillis] is null while the attack is in progress. */
@Parcelize
data class AttackDetailsUiModel(
    val id: String,
    val startEpochMillis: Long,
    val startZoneId: String,
    val endEpochMillis: Long?,
    val nowEpochMillis: Long,
    val intensityPoints: List<IntensityPointUiModel>,
    val peakIntensity: Int?,
    val selectedLocations: Set<HeadLocationUiModel>,
    val symptomChoices: List<SymptomUiModel>,
    val selectedSymptoms: Set<SymptomUiModel>,
    val triggerChoices: List<TriggerUiModel>,
    val selectedTriggers: Set<TriggerUiModel>,
    val functionalImpact: FunctionalImpactUiModel?,
    val note: String,
    val intakes: List<AttackIntakeUiModel> = emptyList()
) : Parcelable
