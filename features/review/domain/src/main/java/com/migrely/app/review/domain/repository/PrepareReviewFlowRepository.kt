package com.migrely.app.review.domain.repository

/** Has Google Play prepare its review flow, and answers whether it did (BRD D130, D131). */
interface PrepareReviewFlowRepository {
    fun prepareReviewFlow(): Boolean
}
