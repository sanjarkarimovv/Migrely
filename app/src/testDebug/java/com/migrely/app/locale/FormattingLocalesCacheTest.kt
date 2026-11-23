package com.migrely.app.locale

import android.app.Application
import android.os.LocaleList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Every formatted date and time asks for the locale, in composition on the main thread, and on Android 13 and newer
 * reading the phone's language list is two binder calls to LocaleManager: the class of "Unresponsive UI" that had
 * 1.0.1 rejected (T23, review of 2d99dabd). The locale is resolved once and kept until the app refreshes it, at start,
 * on a configuration change and when an activity is created.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class FormattingLocalesCacheTest {
    private val application: Application = RuntimeEnvironment.getApplication()

    private var appLanguages = LocaleList.forLanguageTags("de-DE")
    private var reads = 0

    private val classUnderTest = FormattingLocales(
        phoneLanguages = {
            PhoneLanguages(
                appLanguages = {
                    reads++
                    appLanguages
                },
                systemLanguages = {
                    reads++
                    LocaleList.forLanguageTags("en-US")
                }
            )
        }
    )

    @Test
    fun `Given the locale was resolved then later calls read no language list`() {
        classUnderTest.of(application)
        val afterFirst = reads

        repeat(3) { classUnderTest.of(application) }

        assertEquals(afterFirst, reads)
    }

    @Test
    fun `Given the per-app language changed then a refresh picks it up`() {
        assertEquals("de-DE", classUnderTest.of(application).toLanguageTag())
        appLanguages = LocaleList.forLanguageTags("en-US")

        classUnderTest.refresh(application)

        assertEquals("en-US", classUnderTest.of(application).toLanguageTag())
    }

    @Test
    fun `Given the per-app language changed then the activity override follows the refresh`() {
        classUnderTest.of(application)
        appLanguages = LocaleList.forLanguageTags("en-US")

        classUnderTest.refresh(application)

        val override = classUnderTest.activityOverride(application)
        assertEquals("en-US", override.locales[0].toLanguageTag())
    }
}
