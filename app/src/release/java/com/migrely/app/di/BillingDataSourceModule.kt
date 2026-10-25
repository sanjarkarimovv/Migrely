package com.migrely.app.di

import android.content.Context
import com.migrely.app.BuildConfig
import com.migrely.app.activity.CurrentActivityHolder
import com.migrely.app.datasource.billing.datasource.BillingDataSource
import com.migrely.app.datasource.billing.datasource.PlayBillingDataSourceImpl
import com.migrely.app.datasource.billing.mapper.PlayProductDetailsMapper
import com.migrely.app.datasource.billing.mapper.PlayPurchaseMapper
import com.migrely.app.datasource.billing.security.PurchaseSignatureVerifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Release builds always buy through Google Play (BRD D113, design D1), and check each purchase's
 * signature with the licence key; without the key nothing unlocks, and the bundle for Play does not
 * build.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object BillingDataSourceModule {
    @Provides
    @Singleton
    fun providesBillingDataSource(
        @ApplicationContext context: Context,
        currentActivityHolder: CurrentActivityHolder
    ): BillingDataSource = PlayBillingDataSourceImpl(
        context = context,
        currentActivity = currentActivityHolder::resumed,
        playProductDetailsMapper = PlayProductDetailsMapper(),
        playPurchaseMapper = PlayPurchaseMapper(
            PurchaseSignatureVerifier(
                BuildConfig.PLAY_LICENSE_KEY,
                acceptWithoutKey = BuildConfig.DEBUG
            )
        )
    )
}
