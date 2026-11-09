package com.migrely.app.di

import android.content.Context
import android.net.ConnectivityManager
import com.migrely.app.datasource.network.datasource.NetworkStateDataSource
import com.migrely.app.datasource.network.datasource.NetworkStateDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * The phone's network state, read at each tap that would open Google Play (change
 * fix-offline-play-sheet, BRD D166).
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkStateDataSourceModule {
    @Provides
    fun providesNetworkStateDataSource(
        @ApplicationContext context: Context
    ): NetworkStateDataSource =
        NetworkStateDataSourceImpl(context.getSystemService(ConnectivityManager::class.java))
}
