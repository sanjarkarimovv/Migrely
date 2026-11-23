package com.migrely.app.locale

import android.app.Application
import android.app.LocaleManager
import android.content.res.Configuration
import android.os.LocaleList
import com.migrely.app.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Strings read outside an activity follow the app's language, not Android's resource matching (BRD D195, design D2):
 * for ru-RU, en-US, de-DE Android alone would pick `values-de`, while the screens are English.
 *
 * SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class FormattingLocalesTest {
    private val application: Application = RuntimeEnvironment.getApplication()

    private val classUnderTest = FormattingLocales()

    private fun phone(tags: String) = application.createConfigurationContext(
        Configuration(application.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags(tags))
        }
    )

    private fun reminderTitle(tags: String) =
        classUnderTest.localizedContext(phone(tags)).getString(R.string.notification_public_title)

    @Test
    fun `Given English ahead of German then English words`() {
        assertEquals("Reminder", reminderTitle("ru-RU,en-US,de-DE"))
    }

    @Test
    fun `Given German after an unsupported language then German words`() {
        assertEquals("Erinnerung", reminderTitle("ru-RU,de-DE"))
    }

    @Test
    fun `Given an unsupported language alone then English words`() {
        assertEquals("Reminder", reminderTitle("ru-RU"))
    }

    /**
     * T23 8.2 on 5570: an activity restored after its process died is given the configuration it had then. Migrely's
     * language was changed to English while the process was dead; the restored screen stayed German.
     */
    @Test
    fun `Given a stale German configuration and English chosen then the activity is English`() {
        application.getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags("en-US")

        val override = classUnderTest.activityOverride(phone("de-DE,en-US"))

        assertEquals("en-US", override.locales[0].toLanguageTag())
    }

    @Test
    fun `Given a stale English configuration and German chosen then the activity is German`() {
        application.getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags("de-DE")

        val override = classUnderTest.activityOverride(phone("en-US"))

        assertEquals("de-DE", override.locales[0].toLanguageTag())
    }

    @Test
    fun `Given German then the localized context formats in German`() {
        val localized = classUnderTest.localizedContext(phone("ru-RU,de-DE"))

        assertEquals("de-DE", localized.resources.configuration.locales[0].toLanguageTag())
    }
}
