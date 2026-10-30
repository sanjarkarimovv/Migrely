package com.migrely.app.review.domain.repository

import com.migrely.app.review.domain.model.ReviewMarksDomainModel

/** How many times the app has asked for a rating, and when it last did (BRD D131). */
interface GetReviewMarksRepository {
    fun reviewMarks(): ReviewMarksDomainModel
}
