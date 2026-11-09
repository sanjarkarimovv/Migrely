package com.migrely.app.datasource.notification.datasource

import android.app.Notification
import android.app.Application
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Nothing that the lock screen can show reveals the person's health data (change fix-notification-privacy, BRD D169,
 * #178). Every notification is private, with one shared public version, "Reminder", that has no text and no actions.
 *
 * SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export; the visibility API is the same since 21.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class NotificationLockScreenTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val classUnderTest = NotificationDataSourceImpl(context)

    private fun posted(): List<Notification> = manager.activeNotifications.map { it.notification }

    private fun Notification.title() = extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()

    private fun Notification.text() = extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()

    private fun assertPrivate(notification: Notification) {
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, notification.visibility)
        val public = requireNotNull(notification.publicVersion) { "no public version" }
        assertEquals("Reminder", public.title())
        assertNull(public.text())
        assertTrue(public.actions.isNullOrEmpty())
    }

    @Test
    fun `When the daily reminder is posted then it is private with the shared public version`() {
        classUnderTest.showDailyReminder(LocalDate.parse("2026-11-18"))

        assertPrivate(posted().single())
    }

    @Test
    fun `When an attack today stops the pain-free mark then the update names no migraine`() {
        classUnderTest.showAttackLoggedToday()

        val notification = posted().single()
        assertPrivate(notification)
        assertEquals("Not marked. Today already has an entry.", notification.title())
    }

    @Test
    fun `When did it help is posted then it names no medication and is private`() {
        classUnderTest.showEffectQuestion("intake-1")

        val notification = posted().single()
        assertPrivate(notification)
        assertEquals("Did it help?", notification.title())
        assertEquals("Tap to answer in Migrely.", notification.text())
    }
}
