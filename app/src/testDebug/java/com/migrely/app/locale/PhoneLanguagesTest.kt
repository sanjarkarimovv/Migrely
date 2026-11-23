package com.migrely.app.locale

import android.app.Application
import android.content.res.Configuration
import android.os.LocaleList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Which list the app's language is decided from (BRD D195, EC-L10N-06). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PhoneLanguagesTest {
    private val staleGerman = Configuration().apply {
        setLocales(LocaleList.forLanguageTags("de-DE,en-US"))
    }

    private fun tags(languages: PhoneLanguages) =
        languages.of(staleGerman).joinToString(",") { it.toLanguageTag() }

    @Test
    fun `Given a per-app language then it and the system's list, not the configuration`() {
        val languages = PhoneLanguages(
            appLanguages = { LocaleList.forLanguageTags("en-US") },
            systemLanguages = { LocaleList.forLanguageTags("ru-RU,de-DE") }
        )

        assertEquals("en-US,ru-RU,de-DE", tags(languages))
    }

    @Test
    fun `Given Migrely follows the system then the system's list, not the configuration`() {
        val languages = PhoneLanguages(
            appLanguages = { LocaleList.getEmptyLocaleList() },
            systemLanguages = { LocaleList.forLanguageTags("en-US") }
        )

        assertEquals("en-US", tags(languages))
    }

    @Test
    fun `Given no per-app language below Android 13 then the configuration's list`() {
        val languages = PhoneLanguages(
            appLanguages = { null },
            systemLanguages = { LocaleList.forLanguageTags("en-US") }
        )

        assertEquals("de-DE,en-US", tags(languages))
    }
}
