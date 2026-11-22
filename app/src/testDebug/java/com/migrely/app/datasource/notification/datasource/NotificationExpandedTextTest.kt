package com.migrely.app.datasource.notification.datasource

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.migrely.app.locale.FormattingLocales
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * A notification's whole text can be read when it is expanded, so longer German, and English at 200%, is never cut
 * (BRD D195, ux-designer's T23 check E6).
 *
 * SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "de-rDE")
class NotificationExpandedTextTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val classUnderTest = NotificationDataSourceImpl(context, FormattingLocales())

    private fun posted(): Notification = manager.activeNotifications.single().notification

    private fun assertExpandsToItsText(notification: Notification) {
        val text = notification.extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()
        val bigText = notification.extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT)
            ?.toString()

        assertTrue("no text", !text.isNullOrBlank())
        assertEquals(text, bigText)
    }

    @Test
    fun `When the daily reminder is posted then it expands to its whole text`() {
        classUnderTest.showDailyReminder(LocalDate.parse("2026-11-22"), quiet = false)

        assertExpandsToItsText(posted())
    }

    @Test
    fun `When did it help is posted then it expands to its whole text`() {
        classUnderTest.showEffectQuestion("intake-1")

        assertExpandsToItsText(posted())
    }

    @Test
    fun `When the visit reminder is posted then it expands to its whole text`() {
        classUnderTest.showVisitReminder(
            LocalDate.parse("2099-11-20"),
            Instant.now().plusSeconds(3_600)
        )

        assertExpandsToItsText(posted())
    }
}
