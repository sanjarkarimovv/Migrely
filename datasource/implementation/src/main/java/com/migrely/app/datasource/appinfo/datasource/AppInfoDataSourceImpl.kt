package com.migrely.app.datasource.appinfo.datasource

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi

/** The exit reasons that count as a crash (BRD D130, EC-REV-04). */
@RequiresApi(Build.VERSION_CODES.R)
private val CRASH_REASONS = setOf(
    ApplicationExitInfo.REASON_CRASH,
    ApplicationExitInfo.REASON_CRASH_NATIVE,
    ApplicationExitInfo.REASON_ANR
)

/**
 * Reads the installed version of [packageName], the app's own, from Android's package manager, and how its previous run
 * ended from [activityManager].
 */
class AppInfoDataSourceImpl(
    private val packageManager: PackageManager,
    private val packageName: String,
    private val activityManager: ActivityManager?,
    private val sdkInt: () -> Int = { Build.VERSION.SDK_INT }
) : AppInfoDataSource {
    @Suppress("SwallowedException")
    override fun versionName(): String = try {
        val info = if (isAtLeastTiramisu()) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        info.versionName.orEmpty()
    } catch (exception: PackageManager.NameNotFoundException) {
        ""
    }

    /** The newest record is the previous run: this one has not ended yet. */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun previousExitCrashed(): Boolean = try {
        isAtLeastR() &&
            activityManager?.getHistoricalProcessExitReasons(packageName, 0, 1)
                ?.firstOrNull()?.reason in CRASH_REASONS
    } catch (exception: RuntimeException) {
        false
    }

    /** The version check, annotated so that Android Lint sees the guard behind the injected [sdkInt] (QA #74). */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private fun isAtLeastTiramisu() = sdkInt() >= Build.VERSION_CODES.TIRAMISU

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    private fun isAtLeastR() = sdkInt() >= Build.VERSION_CODES.R
}
