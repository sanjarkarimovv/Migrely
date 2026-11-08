package com.migrely.app.review.domain.repository

import java.time.Instant

/** Counts one request for a rating, made [at] (BRD D131). A failed write is dropped. */
interface MarkReviewRequestedRepository {
    fun markReviewRequested(at: Instant)
}
