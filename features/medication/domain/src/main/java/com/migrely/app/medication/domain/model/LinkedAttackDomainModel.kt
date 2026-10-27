package com.migrely.app.medication.domain.model

import java.time.Instant
import java.time.ZoneId

/**
 * The attack an intake is logged for; [end] is null while it is in progress. [zone] is where it started, in which its
 * times are shown and its intakes are picked (BRD EC-TIM-02).
 */
data class LinkedAttackDomainModel(
    val id: String,
    val start: Instant,
    val end: Instant?,
    val zone: ZoneId
)
