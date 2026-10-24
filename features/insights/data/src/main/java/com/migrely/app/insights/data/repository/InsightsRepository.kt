package com.migrely.app.insights.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.insights.data.mapper.InsightsRecordsDomainMapper
import com.migrely.app.insights.domain.model.CurrentTimeDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsResultDomainModel
import com.migrely.app.insights.domain.repository.GetCurrentTimeRepository
import com.migrely.app.insights.domain.repository.GetInsightsRecordsRepository
import com.migrely.app.insights.domain.repository.GetPeriodTrackingRepository

/** Insights (BRD D87–D92): the diary's records, "Track periods" and the clock. Reading never throws. */
class InsightsRepository(
    private val backupDataSource: BackupDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val clockDataSource: ClockDataSource,
    private val insightsRecordsDomainMapper: InsightsRecordsDomainMapper
) : GetCurrentTimeRepository,
    GetInsightsRecordsRepository,
    GetPeriodTrackingRepository {
    override fun currentTime() =
        CurrentTimeDomainModel(clockDataSource.now(), clockDataSource.zone())

    override fun insightsRecords(): InsightsRecordsResultDomainModel = try {
        InsightsRecordsResultDomainModel.Loaded(
            insightsRecordsDomainMapper.toDomain(backupDataSource.records())
        )
    } catch (_: DataException) {
        InsightsRecordsResultDomainModel.Failed
    }

    /** Reading a setting never fails (PreferencesDataSource). */
    override fun periodTracking(): Boolean = preferencesDataSource.periodTracking()
}
