package com.migrely.app.attack.domain.model

import java.time.Instant

data class UndoTimesChangeRequestDomainModel(
    val attackId: String,
    val start: Instant,
    val end: Instant?,
    val points: List<IntensityPointDomainModel>
)
