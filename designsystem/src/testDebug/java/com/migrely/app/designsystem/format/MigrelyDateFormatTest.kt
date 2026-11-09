package com.migrely.app.designsystem.format

import android.app.Application
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
private val TODAY: LocalDate = LocalDate.of(2026, 11, 10)

/**
 * A day as the locale writes it (BRD EC-TIM-05, design D5). SDK 35: Robolectric's Android 16 sandbox needs a JDK
 * internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MigrelyDateFormatTest {
    @Test
    fun `Given en-US when a day this year is shown then the weekday, month and day`() {
        assertEquals("Fri, Nov 20", MigrelyDateFormat.short(NOVEMBER_20, TODAY, Locale.US))
    }

    @Test
    fun `Given en-GB when a day this year is shown then in that region's order`() {
        // The day before the month; the punctuation follows the phone's own locale data.
        val shown = MigrelyDateFormat.short(NOVEMBER_20, TODAY, Locale.UK)
        assertTrue(shown, shown.startsWith("Fri") && shown.endsWith("20 Nov"))
    }

    @Test
    fun `Given a day in another year when shown then with its year`() {
        assertEquals(
            "Fri, Jan 8, 2027",
            MigrelyDateFormat.short(LocalDate.of(2027, 1, 8), TODAY, Locale.US)
        )
    }

    @Test
    fun `When a day is spoken then in full, with the year`() {
        assertEquals("Friday, November 20, 2026", MigrelyDateFormat.spoken(NOVEMBER_20, Locale.US))
    }
}
