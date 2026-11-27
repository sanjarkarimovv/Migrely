package com.migrely.app.review.domain.repository

import com.migrely.app.review.domain.model.LastSafetyCardDomainModel

/** When a safety card last appeared (BRD D188, EC-REV-14). */
interface GetLastSafetyCardShownRepository {
    fun lastSafetyCardShown(): LastSafetyCardDomainModel
}
