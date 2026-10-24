package com.migrely.app.history.domain.model

/**
 * A month's numbers (BRD STA-F1, STA-F2, D53–D56), counted to today in the current month. [averagePeak] is null when
 * no attack of the month has a pain level; [chronic] is true from 15 migraine days; [progress] is null when either
 * month has too few days with entries, when the month before cannot be read, or before any attack is logged.
 */
data class MonthStatsDomainModel(
    val migraines: Int,
    val migraineDays: Int,
    val painFreeDays: Int,
    val averagePeak: PainLevelDomainModel?,
    val attackMedicationDays: Int,
    val chronic: Boolean,
    val progress: ProgressDomainModel?
)
