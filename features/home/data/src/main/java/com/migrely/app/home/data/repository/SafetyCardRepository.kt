package com.migrely.app.home.data.repository

import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.datasource.UnsavedMarksDataSource
import com.migrely.app.home.domain.repository.MarkSafetyCardShownRepository

/** The safety-card mark, kept with the review marks (BRD D188). */
class SafetyCardRepository(
    private val preferencesDataSource: PreferencesDataSource,
    private val unsavedMarksDataSource: UnsavedMarksDataSource,
    private val clockDataSource: ClockDataSource
) : MarkSafetyCardShownRepository {
    override fun markSafetyCardShown() {
        val now = clockDataSource.now().toEpochMilli()
        try {
            preferencesDataSource.markSafetyCardShown(now)
        } catch (_: DataException) {
            // Kept for this process, so no rating follows the card while the storage is full (#278, D188, D197).
            unsavedMarksDataSource.keepSafetyCardShown(now)
        }
    }
}
