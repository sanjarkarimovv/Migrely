package com.migrely.app.attack.presentation.model

import java.time.Instant

/** When another attack ran; [end] is null while it is in progress. */
data class AttackRangePresentationModel(val start: Instant, val end: Instant?)
