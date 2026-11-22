package com.migrely.app.locale

import android.app.Application
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

    @Test
    fun `Given German then the localized context formats in German`() {
        val localized = classUnderTest.localizedContext(phone("ru-RU,de-DE"))

        assertEquals("de-DE", localized.resources.configuration.locales[0].toLanguageTag())
    }
}
