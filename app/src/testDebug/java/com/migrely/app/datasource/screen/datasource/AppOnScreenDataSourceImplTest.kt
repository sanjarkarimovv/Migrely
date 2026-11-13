package com.migrely.app.datasource.screen.datasource

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.os.Process
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Migrely is on screen only while its process is foreground (BRD D176, design D3). On the JVM; the device walk proves
 * what the system reports for a tap start, a job, the lock screen, split screen and the shade (task 2.9).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppOnScreenDataSourceImplTest {
    private val classUnderTest = AppOnScreenDataSourceImpl()

    private fun givenImportance(importance: Int) {
        val activityManager = RuntimeEnvironment.getApplication()
            .getSystemService(ActivityManager::class.java)
        shadowOf(activityManager).setProcesses(
            listOf(
                RunningAppProcessInfo().apply {
                    pid = Process.myPid()
                    this.importance = importance
                }
            )
        )
    }

    @Test
    fun `Given the process is foreground then on screen`() {
        givenImportance(RunningAppProcessInfo.IMPORTANCE_FOREGROUND)

        assertEquals(true, classUnderTest.isOnScreen())
    }

    @Test
    fun `Given a job, a dark screen or the background then not on screen`() {
        listOf(
            RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE,
            RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING,
            RunningAppProcessInfo.IMPORTANCE_SERVICE,
            RunningAppProcessInfo.IMPORTANCE_CACHED
        ).forEach {
            givenImportance(it)

            assertEquals("importance $it", false, classUnderTest.isOnScreen())
        }
    }

    @Test
    fun `Given the system leaves the memory state unfilled then not on screen, so it posts`() {
        // No entry for this process: Robolectric, like a system that answers nothing, fills in no importance.
        shadowOf(
            RuntimeEnvironment.getApplication().getSystemService(ActivityManager::class.java)
        ).setProcesses(emptyList())

        assertEquals(false, classUnderTest.isOnScreen())
    }
}
