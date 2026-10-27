package com.migrely.app.attack.presentation.model

import java.time.Instant

sealed interface TimeRejectionPresentationModel {
    data object InFuture : TimeRejectionPresentationModel

    data object EndBeforeStart : TimeRejectionPresentationModel

    /**
     * The times overlap [attacks], oldest first (BRD EC-ATK-05, EC-ATK-15). [requestedStart] and [requestedEnd] are the
     * refused times, sent back by "Merge them".
     */
    data class Overlaps(
        val attacks: List<AttackRangePresentationModel>,
        val requestedStart: Instant,
        val requestedEnd: Instant?
    ) : TimeRejectionPresentationModel
}
