package com.migrely.app.locale

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * The one [FormattingLocales] the formatters read, for the places that refresh it before Hilt injects them: the
 * application before its `super.onCreate()`, and `MainActivity` as its context is attached (BRD D195).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocaleEntryPoint {
    fun formattingLocales(): FormattingLocales
}
