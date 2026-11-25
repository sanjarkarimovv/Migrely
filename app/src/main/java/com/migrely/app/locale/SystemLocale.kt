package com.migrely.app.locale

import java.util.Locale

/**
 * The system's own language, never Migrely's app language (BRD D72, D195 (5), EC-L10N-10), for the CSV separator: the
 * file opens in a spreadsheet of the phone's region. On Android 13 and newer the system configuration already carries
 * the per-app language, so [appLanguageIgnored] reads LocaleManager's system locales, which ignore it; below Android 13
 * it gives null, there is no per-app language, and [systemConfiguration] is the system's own.
 */
class SystemLocale(
    private val appLanguageIgnored: () -> Locale?,
    private val systemConfiguration: () -> Locale
) {
    fun current(): Locale = appLanguageIgnored() ?: systemConfiguration()
}
