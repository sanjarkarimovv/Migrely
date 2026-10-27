package com.migrely.app.attack.domain.model

import java.time.Instant
import java.time.ZoneId

/** The times a new past attack starts with, shown in [zone] (BRD D26). */
data class PastAttackDraftDomainModel(val start: Instant, val end: Instant, val zone: ZoneId)
