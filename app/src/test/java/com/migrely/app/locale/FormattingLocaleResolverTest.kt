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

/** English names with the phone's region (BRD D117). */
@RunWith(Parameterized::class)
class FormattingLocaleResolverTest(
    private val phone: String,
    private val formatting: String,
    private val firstDay: DayOfWeek
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} formats as {1}, weeks from {2}")
        fun parameters() = listOf(
            arrayOf<Any?>("en-US", "en-US", DayOfWeek.SUNDAY),
            arrayOf<Any?>("en-GB", "en-GB", DayOfWeek.MONDAY),
            arrayOf<Any?>("en", "en", DayOfWeek.SUNDAY),
            arrayOf<Any?>("ru-RU", "en-RU", DayOfWeek.MONDAY),
            arrayOf<Any?>("uz-UZ", "en-UZ", DayOfWeek.MONDAY),
            arrayOf<Any?>("uz-Latn-UZ", "en-UZ", DayOfWeek.MONDAY),
            arrayOf<Any?>("de-DE", "en-DE", DayOfWeek.MONDAY),
            arrayOf<Any?>("pt-BR", "en-BR", DayOfWeek.SUNDAY),
            arrayOf<Any?>("ar-EG", "en-EG", DayOfWeek.SATURDAY),
            // A first day chosen in Android's regional preferences stays.
            arrayOf<Any?>("ru-RU-u-fw-sun", "en-RU-u-fw-sun", DayOfWeek.SUNDAY),
            arrayOf<Any?>("en-US-u-fw-mon", "en-US-u-fw-mon", DayOfWeek.MONDAY)
        )
    }

    private val classUnderTest = FormattingLocaleResolver()

    @Test
    fun `When resolving then English with the phone's region`() {
        val resolved = classUnderTest.resolve(Locale.forLanguageTag(phone))

        assertEquals(formatting, resolved.toLanguageTag())
        assertEquals(firstDay, WeekFields.of(resolved).firstDayOfWeek)
    }
}

class FormattingLocaleResolverNamesTest {
    private val classUnderTest = FormattingLocaleResolver()

    @Test
    fun `Given a Russian phone then English names and weeks from Monday`() {
        val resolved = classUnderTest.resolve(Locale.forLanguageTag("ru-RU"))

        assertEquals(
            "Sunday 25 October",
            DateTimeFormatter.ofPattern("EEEE d MMMM", resolved).format(LocalDate.of(2026, 10, 25))
        )
        assertEquals("M", DayOfWeek.MONDAY.getDisplayName(TextStyle.NARROW, resolved))
        assertEquals(DayOfWeek.MONDAY, WeekFields.of(Locale.forLanguageTag("en-RU")).firstDayOfWeek)
    }

    @Test
    fun `Given no country then plain English, weeks from the phone's day`() {
        val phone = Locale.forLanguageTag("ru")
        val resolved = classUnderTest.resolve(phone)

        assertEquals(Locale.ENGLISH.language, resolved.language)
        assertEquals("", resolved.country)
        assertEquals(WeekFields.of(phone).firstDayOfWeek, WeekFields.of(resolved).firstDayOfWeek)
    }
}
