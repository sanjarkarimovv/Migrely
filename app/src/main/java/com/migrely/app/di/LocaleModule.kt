package com.migrely.app.di

import com.migrely.app.locale.FormattingLocales
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** The formatting locale every date and time formatter reads (BRD D117). */
@Module
@InstallIn(SingletonComponent::class)
internal object LocaleModule {
    @Provides
    @Singleton
    fun providesFormattingLocales() = FormattingLocales()
}
