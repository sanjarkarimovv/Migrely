package com.migrely.app.review.domain.repository

import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel

/** Opens the flow Google Play prepared over the screen and waits for it to end (BRD D129, D131). */
interface LaunchReviewFlowRepository {
    fun launchReviewFlow(): ReviewFlowResultDomainModel
}
