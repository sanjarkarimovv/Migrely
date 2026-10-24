package com.migrely.app.history.domain.model

/** What was logged around a run of days: the attacks touching it, its day marks and its attack-medication intakes. */
data class PeriodEntriesDomainModel(
    val attacks: List<HistoryAttackDomainModel>,
    val marks: List<DayMarkDomainModel>,
    val intakes: List<AttackMedicationIntakeDomainModel>
)
