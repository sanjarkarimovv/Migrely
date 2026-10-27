package com.migrely.app.datasource.medication.model

import java.time.Instant

/** When and in which zone the earliest acute intake was taken (BRD D99). */
data class EarliestAcuteIntakeDataModel(val takenAt: Instant, val takenZoneId: String)
