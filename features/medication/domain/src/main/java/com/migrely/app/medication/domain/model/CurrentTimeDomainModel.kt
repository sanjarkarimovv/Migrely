package com.migrely.app.medication.domain.model

import java.time.Instant
import java.time.ZoneId

data class CurrentTimeDomainModel(val instant: Instant, val zone: ZoneId)
