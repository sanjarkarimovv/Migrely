package com.migrely.app.datasource.appinfo.datasource

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

private const val PACKAGE = "com.migrely.app"

class AppInfoDataSourceImplTest {
    private val packageManager: PackageManager = mock()
    private val activityManager: ActivityManager = mock()

    private fun dataSource(sdkInt: Int) =
        AppInfoDataSourceImpl(packageManager, PACKAGE, activityManager) { sdkInt }

    private fun exit(reason: Int): ApplicationExitInfo = mock {
        on { this.reason } doReturn reason
    }

    private fun crashed(sdkInt: Int = Build.VERSION_CODES.R) =
        dataSource(sdkInt).previousExitCrashed()

    private fun givenLastExit(vararg reasons: Int) {
        // Built before stubbing, as Mockito cannot stub one mock while making another.
        val exits = reasons.map(::exit)
        given(activityManager.getHistoricalProcessExitReasons(PACKAGE, 0, 1)).willReturn(exits)
    }

    private fun info(versionName: String?) = PackageInfo().apply { this.versionName = versionName }

    @Test
    fun `When asked then the installed version, on any Android version`() {
        given(packageManager.getPackageInfo(PACKAGE, 0)).willReturn(info("1.2.0"))
        given(
            packageManager.getPackageInfo(eq(PACKAGE), anyOrNull<PackageManager.PackageInfoFlags>())
        ).willReturn(info("1.3.0"))

        assertEquals(
            "1.2.0",
            dataSource(Build.VERSION_CODES.S).versionName()
        )
        assertEquals(
            "1.3.0",
            dataSource(Build.VERSION_CODES.TIRAMISU).versionName()
        )
    }

    @Test
    fun `Given Android does not say then an empty version`() {
        given(packageManager.getPackageInfo(PACKAGE, 0)).willReturn(info(null))
        assertEquals(
            "",
            dataSource(Build.VERSION_CODES.S).versionName()
        )

        given(packageManager.getPackageInfo(PACKAGE, 0))
            .willThrow(PackageManager.NameNotFoundException())
        assertEquals(
            "",
            dataSource(Build.VERSION_CODES.S).versionName()
        )
    }

    @Test
    fun `Given the previous run crashed, crashed natively or stopped answering then it crashed`() {
        listOf(
            ApplicationExitInfo.REASON_CRASH,
            ApplicationExitInfo.REASON_CRASH_NATIVE,
            ApplicationExitInfo.REASON_ANR
        ).forEach { reason ->
            givenLastExit(reason)

            assertTrue("reason $reason", crashed())
        }
    }

    @Test
    fun `Given the previous run ended normally, or there was none, then it did not crash`() {
        givenLastExit(ApplicationExitInfo.REASON_USER_REQUESTED)
        assertFalse(crashed())

        givenLastExit(ApplicationExitInfo.REASON_EXIT_SELF)
        assertFalse(crashed())

        givenLastExit()
        assertFalse(crashed())
    }

    @Test
    fun `Given Android 10 or older then it never says crashed and does not ask`() {
        assertFalse(crashed(sdkInt = Build.VERSION_CODES.Q))
        verifyNoInteractions(activityManager)
    }

    @Test
    fun `Given Android fails to answer then it did not crash`() {
        given(activityManager.getHistoricalProcessExitReasons(PACKAGE, 0, 1))
            .willThrow(IllegalArgumentException())

        assertFalse(crashed())
    }

    @Test
    fun `Given no activity manager then it did not crash`() {
        assertFalse(
            AppInfoDataSourceImpl(packageManager, PACKAGE, null) {
                Build.VERSION_CODES.R
            }.previousExitCrashed()
        )
    }
}
