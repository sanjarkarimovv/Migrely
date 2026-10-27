package com.migrely.app.di

import android.content.Context
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The app's own settings that are not health records (change add-onboarding, BRD D78), and the
 * premium entitlement in a file of its own that is never backed up (change add-subscription, D115).
 */
@Module
@InstallIn(SingletonComponent::class)
internal object PreferencesDataSourceModule {
    @Provides
    @Singleton
    fun providesPreferencesDataSource(@ApplicationContext context: Context): PreferencesDataSource =
        PreferencesDataSourceImpl(
            preferences = context.getSharedPreferences(
                PreferencesDataSourceImpl.FILE,
                Context.MODE_PRIVATE
            ),
            entitlementPreferences = context.getSharedPreferences(
                PreferencesDataSourceImpl.ENTITLEMENT_FILE,
                Context.MODE_PRIVATE
            )
        )
}
