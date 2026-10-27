package com.migrely.app.datasource.schedule.datasource

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.premium.PremiumCheckWorker
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

private const val CHECK_HOURS = 12L

/**
 * The checks as WorkManager periodic work: one unique work with its own tag, no constraints, so it
 * runs offline too and asks Play when it can (review I2).
 */
class PremiumCheckScheduleDataSourceImpl(private val workManager: WorkManager) :
    PremiumCheckScheduleDataSource {
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<PremiumCheckWorker>(CHECK_HOURS, TimeUnit.HOURS)
            .addTag(PREMIUM_CHECK_TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PREMIUM_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun cancel() {
        try {
            workManager.cancelUniqueWork(PREMIUM_CHECK_WORK).result.get()
        } catch (exception: ExecutionException) {
            throw DatabaseDataException(exception)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw DatabaseDataException(exception)
        }
    }

    companion object {
        const val PREMIUM_CHECK_WORK = "premium-check"
        const val PREMIUM_CHECK_TAG = "premium-check"
    }
}
