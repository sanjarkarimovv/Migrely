package com.migrely.app.attack.domain.model

import java.time.Instant
import java.time.ZoneId

/** Everything stored for one attack. [end] is null while the attack is in progress; [intakes] are oldest first. */
data class AttackDetailsDomainModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant?,
    val intensityPoints: List<IntensityPointDomainModel>,
    val locations: Set<HeadLocationDomainModel>,
    val symptoms: Set<SymptomDomainModel>,
    val triggers: Set<TriggerDomainModel>,
    val functionalImpact: FunctionalImpactDomainModel?,
    val note: String,
    val intakes: List<AttackIntakeDomainModel> = emptyList()
) {
    val isInProgress: Boolean get() = end == null

    val peakIntensity: Int? get() = intensityPoints.maxOfOrNull(IntensityPointDomainModel::value)

    val range: AttackRangeDomainModel get() = AttackRangeDomainModel(start, end)
}
