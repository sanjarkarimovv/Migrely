package com.migrely.app.locale

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * The first supported language of the phone's list decides; German keeps its own locale, anything else gets English
 * with the phone's region (BRD D117, D195, design D1's table).
 */
@RunWith(Parameterized::class)
class FormattingLocaleResolverTest(
    private val phone: String,
    private val formatting: String,
    private val firstDay: DayOfWeek
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "[{0}] formats as {1}, weeks from {2}")
        fun parameters() = listOf(
            arrayOf<Any?>("en-US", "en-US", DayOfWeek.SUNDAY),
            arrayOf<Any?>("en-GB", "en-GB", DayOfWeek.MONDAY),
            arrayOf<Any?>("en", "en", DayOfWeek.SUNDAY),
            arrayOf<Any?>("ru-RU", "en-RU", DayOfWeek.MONDAY),
            arrayOf<Any?>("uz-UZ", "en-UZ", DayOfWeek.MONDAY),
            arrayOf<Any?>("uz-Latn-UZ", "en-UZ", DayOfWeek.MONDAY),
            arrayOf<Any?>("pt-BR", "en-BR", DayOfWeek.SUNDAY),
            arrayOf<Any?>("ar-EG", "en-EG", DayOfWeek.SATURDAY),
            // A first day chosen in Android's regional preferences stays.
            arrayOf<Any?>("ru-RU-u-fw-sun", "en-RU-u-fw-sun", DayOfWeek.SUNDAY),
            arrayOf<Any?>("en-US-u-fw-mon", "en-US-u-fw-mon", DayOfWeek.MONDAY),
            // German (D195): the German locale itself.
            arrayOf<Any?>("de-DE", "de-DE", DayOfWeek.MONDAY),
            arrayOf<Any?>("de-AT", "de-AT", DayOfWeek.MONDAY),
            arrayOf<Any?>("de-CH", "de-CH", DayOfWeek.MONDAY),
            // German after an unsupported language: German, with the first language's first day.
            arrayOf<Any?>("ru-RU,de-DE", "de-DE", DayOfWeek.MONDAY),
            arrayOf<Any?>("pt-BR,de-DE", "de-DE-u-fw-sun", DayOfWeek.SUNDAY),
            // English ahead of German: today's rule on the first language, unchanged.
            arrayOf<Any?>("ru-RU,en-US,de-DE", "en-RU", DayOfWeek.MONDAY),
            arrayOf<Any?>("en-DE", "en-DE", DayOfWeek.MONDAY),
            // Android 13's app language comes first in the configuration's list.
            arrayOf<Any?>("en-US,de-DE", "en-US", DayOfWeek.SUNDAY),
            arrayOf<Any?>("de-DE,en-US", "de-DE", DayOfWeek.MONDAY),
            // A regional Sunday in German.
            arrayOf<Any?>("de-DE-u-fw-sun", "de-DE-u-fw-sun", DayOfWeek.SUNDAY),
            arrayOf<Any?>("ru-RU-u-fw-sun,de-AT", "de-AT-u-fw-sun", DayOfWeek.SUNDAY)
        )
    }

    private val classUnderTest = FormattingLocaleResolver()

    @Test
    fun `When resolving then the first supported language with the phone's region`() {
        val resolved = classUnderTest.resolve(phone.split(",").map(Locale::forLanguageTag))

        assertEquals(formatting, resolved.toLanguageTag())
        assertEquals(firstDay, WeekFields.of(resolved).firstDayOfWeek)
    }
}

class FormattingLocaleResolverNamesTest {
    private val classUnderTest = FormattingLocaleResolver()

    @Test
    fun `Given a Russian phone then English names and weeks from Monday`() {
        val resolved = classUnderTest.resolve(listOf(Locale.forLanguageTag("ru-RU")))

        assertEquals(
            "Sunday 25 October",
            DateTimeFormatter.ofPattern("EEEE d MMMM", resolved).format(LocalDate.of(2026, 10, 25))
        )
        assertEquals("M", DayOfWeek.MONDAY.getDisplayName(TextStyle.NARROW, resolved))
        assertEquals(DayOfWeek.MONDAY, WeekFields.of(Locale.forLanguageTag("en-RU")).firstDayOfWeek)
    }

    @Test
    fun `Given a German phone then German names`() {
        val resolved = classUnderTest.resolve(listOf(Locale.forLanguageTag("de-DE")))

        assertEquals(
            "Sonntag, 22. November",
            DateTimeFormatter.ofPattern("EEEE, d. MMMM", resolved)
                .format(LocalDate.of(2026, 11, 22))
        )
    }

    @Test
    fun `Given no country then plain English, weeks from the phone's day`() {
        val phone = Locale.forLanguageTag("ru")
        val resolved = classUnderTest.resolve(listOf(phone))

        assertEquals(Locale.ENGLISH.language, resolved.language)
        assertEquals("", resolved.country)
        assertEquals(WeekFields.of(phone).firstDayOfWeek, WeekFields.of(resolved).firstDayOfWeek)
    }

    @Test
    fun `Given German with no country then plain German, weeks from the phone's day`() {
        val phone = Locale.forLanguageTag("de")
        val resolved = classUnderTest.resolve(listOf(phone))

        assertEquals(phone, resolved)
        assertEquals(WeekFields.of(phone).firstDayOfWeek, WeekFields.of(resolved).firstDayOfWeek)
    }

    @Test
    fun `Given an empty list then the default locale's rule`() {
        val resolved = classUnderTest.resolve(emptyList())

        assertEquals(classUnderTest.resolve(listOf(Locale.getDefault())), resolved)
    }
}
