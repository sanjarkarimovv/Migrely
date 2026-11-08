package com.migrely.app.review.data.mapper

import com.migrely.app.datasource.review.model.ReviewFlowDataModel
import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel

/** How Google Play's review flow went, for the domain (BRD D131). */
class ReviewFlowDomainMapper {
    fun toDomain(flow: ReviewFlowDataModel): ReviewFlowResultDomainModel = when (flow) {
        ReviewFlowDataModel.Completed -> ReviewFlowResultDomainModel.Completed
        ReviewFlowDataModel.Unavailable -> ReviewFlowResultDomainModel.Unavailable
    }
}
