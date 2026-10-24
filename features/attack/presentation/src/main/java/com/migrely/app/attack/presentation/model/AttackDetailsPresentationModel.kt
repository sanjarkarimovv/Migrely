package com.migrely.app.attack.presentation.model

import java.time.Instant
import java.time.ZoneId

/**
 * What the details screen shows. The choice lists are complete: built-in choices, the user's active custom tags, and
 * archived custom tags that this attack still uses (BRD EC-ATK-11).
 */
data class AttackDetailsPresentationModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant?,
    val now: Instant,
    val intensityPoints: List<IntensityPointPresentationModel>,
    val peakIntensity: Int?,
    val selectedLocations: Set<HeadLocationPresentationModel>,
    val symptomChoices: List<SymptomPresentationModel>,
    val selectedSymptoms: Set<SymptomPresentationModel>,
    val triggerChoices: List<TriggerPresentationModel>,
    val selectedTriggers: Set<TriggerPresentationModel>,
    val functionalImpact: FunctionalImpactPresentationModel?,
    val note: String
)
