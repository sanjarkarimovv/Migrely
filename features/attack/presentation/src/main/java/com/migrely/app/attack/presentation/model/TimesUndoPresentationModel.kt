package com.migrely.app.attack.presentation.model

import java.time.Instant

/** What a time change replaced, so "Undo" can put it back (BRD EC-ATK-09). */
data class TimesUndoPresentationModel(
    val attackId: String,
    val start: Instant,
    val end: Instant?,
    val removedPoints: List<IntensityPointPresentationModel>
)
