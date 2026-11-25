package com.migrely.app.datasource.notification.datasource

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.migrely.app.locale.FormattingLocales
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val DATE = LocalDate.parse("2026-11-18")

/**
 * A late daily reminder posts without sound or vibration and is otherwise the same post (BRD D183, EC-REM-29). androidx
 * core 1.17.0 makes `setSilent` a group trick on every API from 26: with no group of its own, the post joins the group
 * "silent" as a child that lets only a summary alert, and loses any sound and vibration. There is no summary, so nothing
 * in that group alerts; the post keeps its icon, text, actions and lock screen version.
 *
 * SDK 26 is the app's lowest; SDK 35 as in [NotificationLockScreenTest].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35], application = Application::class)
class NotificationQuietDailyTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val classUnderTest = NotificationDataSourceImpl(context, FormattingLocales())

    private fun posted(quiet: Boolean): Notification {
        classUnderTest.showDailyReminder(DATE, quiet)
        return manager.activeNotifications.single().notification
    }

    private fun Notification.title() =
        extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()

    private fun Notification.text() =
        extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()

    private fun assertSameReminder(notification: Notification) {
        assertEquals("daily", notification.channelId)
        assertNotNull(notification.smallIcon)
        assertEquals("How was today?", notification.title())
        assertEquals("Mark a pain-free day, or log a migraine.", notification.text())
        assertEquals(
            listOf("Pain-free day", "Log a migraine"),
            notification.actions.map { it.title.toString() }
        )
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, notification.visibility)
        assertEquals("Reminder", notification.publicVersion.title())
        assertEquals(0, notification.flags and Notification.FLAG_GROUP_SUMMARY)
    }

    @Test
    fun `Given quiet when posted then androidx's silent group, the rest unchanged`() {
        val notification = posted(quiet = true)

        assertEquals("silent", notification.group)
        assertEquals(Notification.GROUP_ALERT_SUMMARY, notification.groupAlertBehavior)
        assertNull(notification.sound)
        assertNull(notification.vibrate)
        assertEquals(
            0,
            notification.defaults and (Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        )
        assertSameReminder(notification)
    }

    @Test
    fun `Given not quiet when posted then no group, it alerts as its channel says`() {
        val notification = posted(quiet = false)

        assertNull(notification.group)
        assertEquals(Notification.GROUP_ALERT_ALL, notification.groupAlertBehavior)
        assertSameReminder(notification)
    }
}
