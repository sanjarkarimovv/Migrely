package com.migrely.app.insights.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.insights.domain.model.InsightsDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsResultDomainModel
import com.migrely.app.insights.domain.model.InsightsResultDomainModel
import com.migrely.app.insights.domain.repository.GetCurrentTimeRepository
import com.migrely.app.insights.domain.repository.GetInsightsRecordsRepository
import com.migrely.app.insights.domain.repository.GetPeriodTrackingRepository

/**
 * Insights for the last `months` months, the request, today included (BRD INS-F1–F6, EC-INS-01–03, D87–D92): the
 * sections once there is enough to go on, and the last 12 calendar months either way.
 */
class GetInsightsUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getInsightsRecordsRepository: GetInsightsRecordsRepository,
    private val getPeriodTrackingRepository: GetPeriodTrackingRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Int, InsightsResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Int): InsightsResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val dates = lastMonthsOn(request, now.instant.atZone(now.zone).toLocalDate())
        val records = when (val read = getInsightsRecordsRepository.insightsRecords()) {
            is InsightsRecordsResultDomainModel.Loaded -> read.records
            InsightsRecordsResultDomainModel.Failed -> return InsightsResultDomainModel.Failed
        }
        val calculator = InsightsCalculator(dates, now.instant)
        return InsightsResultDomainModel.Ready(
            InsightsDomainModel(
                months = request,
                dates = dates,
                patterns = calculator.patterns(
                    records,
                    getPeriodTrackingRepository.periodTracking()
                ),
                trends = calculator.trends(records)
            )
        )
    }
}
