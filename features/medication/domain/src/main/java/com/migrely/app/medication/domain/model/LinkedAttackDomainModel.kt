package com.migrely.app.medication.domain.model

import java.time.Instant

/** The attack an intake is logged for; [end] is null while it is in progress. */
data class LinkedAttackDomainModel(val id: String, val start: Instant, val end: Instant?)
