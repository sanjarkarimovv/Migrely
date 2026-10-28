package com.migrely.app.review.domain.repository

import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel

/** Runs Google Play's review flow over the screen and waits for it to end (BRD D129, D131). */
interface LaunchReviewFlowRepository {
    fun launchReviewFlow(): ReviewFlowResultDomainModel
}
