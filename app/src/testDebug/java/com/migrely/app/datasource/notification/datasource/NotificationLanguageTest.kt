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
 * Notifications and their channel names speak the app's language at the moment they are shown (BRD D195, EC-L10N-07,
 * EC-L10N-19, design D2). Only channels that already exist are renamed, so none appears early.
 *
 * SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class NotificationLanguageTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val formattingLocales = FormattingLocales()
    private val classUnderTest = NotificationDataSourceImpl(context, formattingLocales)

    @Test
    fun `Given English resolved when German comes with no refresh then the reminder is German`() {
        formattingLocales.of(context)
        RuntimeEnvironment.setQualifiers("de-rDE")

        classUnderTest.showDailyReminder(LocalDate.parse("2026-11-24"), quiet = false)

        assertEquals("Wie war dein Tag?", posted().title())
    }

    private fun posted(): Notification = manager.activeNotifications.single().notification

    private fun Notification.title() =
        extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()

    @Test
    @Config(qualifiers = "de-rDE")
    fun `Given German when the daily reminder is posted then its words are German`() {
        classUnderTest.showDailyReminder(LocalDate.parse("2026-11-22"), quiet = false)

        val notification = posted()
        assertEquals("Wie war dein Tag?", notification.title())
        assertEquals(
            listOf("Schmerzfreier Tag", "Migräne notieren"),
            notification.actions.map { it.title.toString() }
        )
        assertEquals("Erinnerung", notification.publicVersion.title())
        assertEquals("Tägliche Erinnerung", manager.getNotificationChannel("daily").name)
    }

    @Test
    @Config(qualifiers = "ru-rRU")
    fun `Given an unsupported language when the visit reminder is posted then English dates`() {
        classUnderTest.showVisitReminder(
            LocalDate.parse("2099-11-20"),
            Instant.now().plusSeconds(3_600)
        )

        val title = requireNotNull(posted().title())
        assertTrue(title, title.startsWith("Your visit is on ") && "Nov" in title)
    }

    @Test
    fun `Given no channel when the names are refreshed then no channel is created`() {
        classUnderTest.refreshChannelNames()

        assertTrue(manager.notificationChannels.isEmpty())
    }

    @Test
    fun `Given an English channel when the language becomes German then the refresh renames it`() {
        classUnderTest.isVisitChannelOff()
        assertEquals("Doctor visit", manager.getNotificationChannel("visit").name)

        RuntimeEnvironment.setQualifiers("de-rDE")
        // No refresh first: the channel names read the language fresh, as the notifications do (T23 8.2 row 9d).
        classUnderTest.refreshChannelNames()

        val channel = manager.getNotificationChannel("visit")
        assertEquals("Arzttermin", channel.name)
        assertEquals("Eine Erinnerung zwei Tage vor deinem nächsten Termin", channel.description)
        assertEquals(listOf("visit"), manager.notificationChannels.map { it.id })
    }
}
