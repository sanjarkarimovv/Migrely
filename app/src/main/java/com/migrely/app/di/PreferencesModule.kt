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

private const val PREFERENCES = "migrely_preferences"

/** The app's own settings that are not health records (change add-onboarding, BRD D78). */
@Module
@InstallIn(SingletonComponent::class)
internal object PreferencesModule {
    @Provides
    @Singleton
    fun providesPreferencesDataSource(@ApplicationContext context: Context): PreferencesDataSource =
        PreferencesDataSourceImpl(context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE))
}
