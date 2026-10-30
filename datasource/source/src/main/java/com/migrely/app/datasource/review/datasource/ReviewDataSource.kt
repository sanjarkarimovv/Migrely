package com.migrely.app.datasource.review.datasource

import com.migrely.app.datasource.review.model.ReviewFlowDataModel

/**
 * Google Play's in-app review, in two steps, so a caller can drop a flow it no longer wants before it shows (BRD D129,
 * D130, D131). Both functions block until Google Play answers, so they must be called off the main thread, and never
 * throw for anything Google Play does.
 */
interface ReviewDataSource {
    /** Has Google Play prepare the flow, and keeps it for [launchReviewFlow]. Answers whether Google Play did. */
    fun prepareReviewFlow(): Boolean

    /**
     * Opens the prepared flow over the screen and waits for it to end. Uses the prepared flow up, so without one it is
     * [ReviewFlowDataModel.Unavailable].
     */
    fun launchReviewFlow(): ReviewFlowDataModel
}
