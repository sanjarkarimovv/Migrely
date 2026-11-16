package com.migrely.app.home.data.repository

import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.home.domain.repository.MarkSafetyCardShownRepository

/** The safety-card mark, kept with the review marks (BRD D188). */
class SafetyCardRepository(
    private val preferencesDataSource: PreferencesDataSource,
    private val clockDataSource: ClockDataSource
) : MarkSafetyCardShownRepository {
    override fun markSafetyCardShown() {
        try {
            preferencesDataSource.markSafetyCardShown(clockDataSource.now().toEpochMilli())
        } catch (_: DataException) {
            // Dropped: at worst a rating could be asked for within the 3 days (design D4).
        }
    }
}
