package com.migrely.app.attack.presentation.model

import java.time.Instant
import java.time.ZoneId

/** The times the add screen starts with, shown in [zone]. */
data class PastAttackDraftPresentationModel(val start: Instant, val end: Instant, val zone: ZoneId)
