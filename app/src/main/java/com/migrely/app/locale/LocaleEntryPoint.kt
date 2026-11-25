package com.migrely.app.locale

import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * The one [FormattingLocales] the formatters read, for the places that refresh it before Hilt injects them: the
 * application before its `super.onCreate()`, and `MainActivity` as its context is attached (BRD D195); and the
 * notifications, whose channel names follow a language that `MainActivity` finds changed with no configuration change.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocaleEntryPoint {
    fun formattingLocales(): FormattingLocales

    fun notificationDataSource(): NotificationDataSource
}
