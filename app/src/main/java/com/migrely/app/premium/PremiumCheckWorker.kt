package com.migrely.app.premium

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.migrely.app.subscription.domain.model.RefreshPremiumRequestDomainModel
import dagger.hilt.android.EntryPointAccessors

/**
 * Checks Premium with Google Play in the background while a payment is pending or a purchase is
 * not yet acknowledged, so it is acknowledged within Play's 3 days even if Migrely stays closed
 * (review I2). The check itself cancels this work once neither holds.
 */
class PremiumCheckWorker(context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {
    private val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PremiumEntryPoint::class.java
    )

    // The check never throws; one that cannot reach Play waits for the next run.
    override fun doWork(): Result {
        entryPoint.refreshPremiumUseCase()
            .executeInBackground(RefreshPremiumRequestDomainModel(force = true))
        return Result.success()
    }
}
