package com.migrely.app.attack.domain.model

import java.time.Instant

sealed interface OverlapLookupResultDomainModel {
    /** Another attack overlaps; [end] is null when it is still in progress. */
    data class Found(val start: Instant, val end: Instant?) : OverlapLookupResultDomainModel

    data object None : OverlapLookupResultDomainModel

    data object Failed : OverlapLookupResultDomainModel
}
