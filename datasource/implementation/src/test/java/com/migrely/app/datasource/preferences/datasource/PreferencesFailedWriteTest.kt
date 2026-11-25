package com.migrely.app.datasource.preferences.datasource

import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.datasource.preferences.model.ReviewRequestsDataModel
import com.migrely.app.datasource.preferences.model.ThemeDataModel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

/** Long enough for the other thread, short enough that a lock that holds it back only delays the test. */
private const val WAIT_MILLIS = 1_000L

/**
 * A write that does not reach the file leaves every setting as stored (#278, BRD EC-STO-13): the switch then shows the
 * stored value next to "Couldn't save that. Try again.", and a later write of another key cannot save the failed change.
 */
class PreferencesFailedWriteTest {
    private val preferences = MemoryFirstPreferences(
        mapOf(
            "period_tracking" to true,
            "theme" to "light",
            "review_request_count" to 1,
            "review_last_requested" to 5L
        )
    )
    private val entitlementPreferences = MemoryFirstPreferences()
    private val classUnderTest = PreferencesDataSourceImpl(preferences, entitlementPreferences)

    @Test
    fun `Given the file cannot be written when a switch changes then it still reads as stored`() {
        preferences.diskWorks = false

        assertThrows(FileDataException::class.java) { classUnderTest.setPeriodTracking(false) }

        assertEquals(true, classUnderTest.periodTracking())
    }

    @Test
    fun `Given the file cannot be written when a choice changes then it still reads as stored`() {
        preferences.diskWorks = false

        assertThrows(FileDataException::class.java) { classUnderTest.setTheme(ThemeDataModel.Dark) }

        assertEquals(ThemeDataModel.Light, classUnderTest.theme())
    }

    @Test
    fun `Given the file cannot be written when a key not stored yet is set then it stays unset`() {
        preferences.diskWorks = false

        assertThrows(FileDataException::class.java) { classUnderTest.finishOnboarding() }
        assertThrows(FileDataException::class.java) {
            classUnderTest.setGoal(GoalDataModel.entries.first())
        }

        assertFalse(classUnderTest.onboardingFinished())
        assertEquals(null, classUnderTest.goal())
    }

    @Test
    fun `Given the file cannot be written when two keys change in one write then both as stored`() {
        preferences.diskWorks = false

        assertThrows(FileDataException::class.java) { classUnderTest.markReviewRequested(9L) }

        assertEquals(
            ReviewRequestsDataModel(count = 1, lastMillis = 5L),
            classUnderTest.reviewRequests()
        )
    }

    @Test
    fun `Given the file cannot be written when clearing then the settings read as stored`() {
        preferences.diskWorks = false

        assertThrows(FileDataException::class.java) { classUnderTest.clear() }

        assertEquals(ThemeDataModel.Light, classUnderTest.theme())
        assertEquals(true, classUnderTest.periodTracking())
    }

    @Test
    fun `Given a failed write when a later write succeeds then the failed change is not saved`() {
        preferences.diskWorks = false
        assertThrows(FileDataException::class.java) { classUnderTest.setPeriodTracking(false) }

        preferences.diskWorks = true
        classUnderTest.setTheme(ThemeDataModel.Dark)

        assertEquals(true, preferences.disk["period_tracking"])
        assertEquals("dark", preferences.disk["theme"])
    }

    @Test
    fun `Given the file can be written then the change is stored and read back`() {
        classUnderTest.setPeriodTracking(false)

        assertEquals(false, classUnderTest.periodTracking())
        assertEquals(false, preferences.disk["period_tracking"])
    }

    @Test
    fun `Given two failed writes that overlap then neither brings back the other's change`() {
        preferences.diskWorks = false
        val aInMemory = CountDownLatch(1)
        val bSnapshot = CountDownLatch(1)
        val aDone = CountDownLatch(1)
        lateinit var b: Thread
        val a = Thread {
            runCatching { classUnderTest.setPeriodTracking(false) }
            aDone.countDown()
        }
        // The order without a lock: B reads the map after A changed it, and puts its copy back after A restored.
        preferences.afterMemoryChange = {
            when (Thread.currentThread()) {
                a -> if (aInMemory.count > 0) {
                    aInMemory.countDown()
                    bSnapshot.await(WAIT_MILLIS, TimeUnit.MILLISECONDS)
                }
                b -> aDone.await(WAIT_MILLIS, TimeUnit.MILLISECONDS)
            }
        }
        preferences.onGetAll = { if (Thread.currentThread() == b) bSnapshot.countDown() }
        b = Thread { runCatching { classUnderTest.setTheme(ThemeDataModel.Dark) } }

        a.start()
        aInMemory.await(WAIT_MILLIS, TimeUnit.MILLISECONDS)
        b.start()
        a.join()
        b.join()

        assertEquals(true, classUnderTest.periodTracking())
        assertEquals(ThemeDataModel.Light, classUnderTest.theme())
    }
}
