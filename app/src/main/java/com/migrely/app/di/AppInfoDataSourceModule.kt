package com.migrely.app.di

import android.app.ActivityManager
import android.content.Context
import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * The installed app's own facts: its version (change add-settings-and-safety, BRD D97) and how its
 * previous run ended (change add-in-app-review, BRD D130).
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppInfoDataSourceModule {
    @Provides
    fun providesAppInfoDataSource(@ApplicationContext context: Context): AppInfoDataSource =
        AppInfoDataSourceImpl(
            context.packageManager,
            context.packageName,
            context.getSystemService(ActivityManager::class.java)
        )
}
