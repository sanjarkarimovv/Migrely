package com.migrely.app.di

import android.content.Context
import com.migrely.app.activity.CurrentActivityHolder
import com.migrely.app.datasource.billing.datasource.BillingDataSource
import com.migrely.app.datasource.billing.datasource.DialogSimulatedSheet
import com.migrely.app.datasource.billing.datasource.simulatedStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Debug builds buy from the simulated store, never from Google Play (BRD D113, design D1). */
@Module
@InstallIn(SingletonComponent::class)
internal object BillingDataSourceModule {
    @Provides
    @Singleton
    fun providesBillingDataSource(
        @ApplicationContext context: Context,
        currentActivityHolder: CurrentActivityHolder
    ): BillingDataSource =
        simulatedStore(context, DialogSimulatedSheet(currentActivityHolder::resumed))
}
