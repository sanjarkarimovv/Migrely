package com.migrely.app.locale

import java.text.DecimalFormatSymbols
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * The CSV separator follows the system's language, never Migrely's own app language (BRD D72, D195 (5), EC-L10N-10).
 * On Android 13 and newer the system configuration already carries the per-app language, so only LocaleManager's
 * system locales are free of it; below Android 13 there is no per-app language and the configuration is right.
 */
@RunWith(Parameterized::class)
class SystemLocaleTest(
    private val case: String,
    private val appLanguageIgnored: String?,
    private val systemConfiguration: String,
    private val expected: String,
    private val decimalSeparator: Char
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters() = listOf(
            // Android 13+: the configuration holds the app's language, LocaleManager the system's.
            arrayOf<Any?>("33+ system en-US, app de: comma", "en-US", "de-DE", "en-US", '.'),
            arrayOf<Any?>("33+ system de-DE, app en: semicolon", "de-DE", "en-US", "de-DE", ','),
            // Below Android 13: no per-app language, the configuration is the system's.
            arrayOf<Any?>("26-32 system en-US: comma", null, "en-US", "en-US", '.'),
            arrayOf<Any?>("26-32 system de-DE: semicolon", null, "de-DE", "de-DE", ',')
        )
    }

    @Test
    fun `When reading the system's locale then Migrely's app language is ignored`() {
        val classUnderTest = SystemLocale(
            appLanguageIgnored = { appLanguageIgnored?.let(Locale::forLanguageTag) },
            systemConfiguration = { Locale.forLanguageTag(systemConfiguration) }
        )

        val locale = classUnderTest.current()

        assertEquals(expected, locale.toLanguageTag())
        // BackupRepository writes semicolons where the decimal separator is a comma (D72).
        assertEquals(decimalSeparator, DecimalFormatSymbols.getInstance(locale).decimalSeparator)
    }
}
