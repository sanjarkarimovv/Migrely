package com.migrely.app.insights.domain.repository

import com.migrely.app.insights.domain.model.InsightsRecordsResultDomainModel

/** Every health record but attacks being deleted; Insights keeps what it needs. */
interface GetInsightsRecordsRepository {
    fun insightsRecords(): InsightsRecordsResultDomainModel
}
