package com.migrely.app.datasource.notification.datasource

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.migrely.app.locale.FormattingLocales
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val HEALTH_WORDS = listOf("arzt", "migräne", "kopfschmerz", "premium")

/**
 * The German twin of [NotificationLockScreenTest] (BRD D168, D169, D195): every notification is private with the
 * shared public version "Erinnerung", and the visit reminder, which can show on a lock screen, names no health word.
 *
 * SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "de-rDE")
class NotificationLockScreenGermanTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val classUnderTest = NotificationDataSourceImpl(context, FormattingLocales())

    private fun posted(): Notification = manager.activeNotifications.single().notification

    private fun Notification.title() =
        extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()

    private fun Notification.text() =
        extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()

    private fun assertPrivate(notification: Notification) {
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, notification.visibility)
        val public = requireNotNull(notification.publicVersion) { "no public version" }
        assertEquals("Erinnerung", public.title())
        assertNull(public.text())
        assertTrue(public.actions.isNullOrEmpty())
    }

    private fun assertNoHealthWord(vararg texts: String?) {
        val shown = texts.filterNotNull().joinToString(" ").lowercase()
        HEALTH_WORDS.forEach { word -> assertFalse("\"$word\" shows: $shown", word in shown) }
    }

    @Test
    fun `When the daily reminder is posted then it is private with the shared public version`() {
        classUnderTest.showDailyReminder(LocalDate.parse("2026-11-18"), quiet = false)

        val notification = posted()
        assertPrivate(notification)
        assertNoHealthWord(notification.publicVersion.title(), notification.publicVersion.text())
    }

    @Test
    fun `When an attack today stops the pain-free mark then the update names no migraine`() {
        classUnderTest.showAttackLoggedToday()

        val notification = posted()
        assertPrivate(notification)
        assertNoHealthWord(notification.title(), notification.publicVersion.title())
    }

    @Test
    fun `When did it help is posted then it names no medication and is private`() {
        classUnderTest.showEffectQuestion("intake-1")

        val notification = posted()
        assertPrivate(notification)
        assertNoHealthWord(
            notification.title(),
            notification.text(),
            notification.publicVersion.title()
        )
    }

    @Test
    fun `When the visit reminder is posted then it is private and names no health word`() {
        classUnderTest.showVisitReminder(
            LocalDate.parse("2099-11-20"),
            Instant.now().plusSeconds(3_600)
        )

        val notification = posted()
        assertPrivate(notification)
        assertTrue(notification.actions.isNullOrEmpty())
        assertNoHealthWord(
            notification.title(),
            notification.text(),
            notification.publicVersion.title(),
            notification.publicVersion.text()
        )
    }
}
