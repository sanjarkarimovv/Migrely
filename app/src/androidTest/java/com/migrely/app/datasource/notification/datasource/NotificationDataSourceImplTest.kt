package com.migrely.app.datasource.notification.datasource

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val WAIT_MILLIS = 5_000L
private const val POLL_MILLIS = 50L

/**
 * Removing every notification before the app starts again (change add-settings-and-safety, BRD D96). It posts through
 * the app's own channels, so Android 13 and later grant the notification permission first.
 */
@RunWith(AndroidJUnit4::class)
class NotificationDataSourceImplTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val classUnderTest = NotificationDataSourceImpl(context)

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        manager.cancelAll()
        // A phone where the user turned Migrely's notifications off posts nothing, so there is nothing to remove.
        assumeTrue(classUnderTest.areEnabled())
    }

    @After
    fun tearDown() {
        manager.cancelAll()
    }

    private fun waitForActive(count: Int) {
        val until = SystemClock.uptimeMillis() + WAIT_MILLIS
        while (manager.activeNotifications.size != count && SystemClock.uptimeMillis() < until) {
            SystemClock.sleep(POLL_MILLIS)
        }
    }

    @Test
    fun whenEverythingIsCancelled_thenNoMigrelyNotificationStays() {
        classUnderTest.showDailyReminder(LocalDate.now())
        classUnderTest.showEffectQuestion("intake-1", "Ibuprofen")
        waitForActive(2)
        assertTrue(manager.activeNotifications.size >= 2)

        classUnderTest.cancelAll()
        waitForActive(0)

        assertEquals(0, manager.activeNotifications.size)
    }
}
