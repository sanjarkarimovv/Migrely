package com.migrely.app.di

import android.content.Context
import androidx.work.WorkManager
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/** Background checks of Premium while a payment is pending or unacknowledged (review I2). */
@Module
@InstallIn(SingletonComponent::class)
internal object PremiumCheckScheduleDataSourceModule {
    @Provides
    fun providesPremiumCheckScheduleDataSource(
        @ApplicationContext context: Context
    ): PremiumCheckScheduleDataSource =
        PremiumCheckScheduleDataSourceImpl(WorkManager.getInstance(context))
}
