package com.migrely.app.datasource.review.datasource

import com.migrely.app.datasource.review.model.ReviewFlowDataModel

/** Google Play's in-app review (BRD D129, D131). */
interface ReviewDataSource {
    /**
     * Runs the review flow over the screen and waits for it to end. Blocks, so it must be called off the main thread.
     * Never throws: whatever goes wrong is [ReviewFlowDataModel.Unavailable].
     */
    fun launchReviewFlow(): ReviewFlowDataModel
}
