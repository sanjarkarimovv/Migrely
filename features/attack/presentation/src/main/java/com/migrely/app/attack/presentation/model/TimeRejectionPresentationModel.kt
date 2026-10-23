package com.migrely.app.attack.presentation.model

import java.time.Instant

sealed interface TimeRejectionPresentationModel {
    data object InFuture : TimeRejectionPresentationModel

    data object EndBeforeStart : TimeRejectionPresentationModel

    /** Another attack ran from [start] to [end]; [end] is null when it is in progress. */
    data class Overlaps(val start: Instant, val end: Instant?) : TimeRejectionPresentationModel
}
