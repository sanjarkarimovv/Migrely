package com.migrely.app.datasource.attack.model

import java.time.Instant

/** When and in which zone the earliest attack started. */
data class EarliestAttackDataModel(val start: Instant, val startZoneId: String)
