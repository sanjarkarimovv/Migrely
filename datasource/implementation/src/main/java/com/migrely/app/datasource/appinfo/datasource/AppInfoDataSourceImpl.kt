package com.migrely.app.datasource.appinfo.datasource

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/** Reads the installed version of [packageName], the app's own, from Android's package manager. */
class AppInfoDataSourceImpl(
    private val packageManager: PackageManager,
    private val packageName: String,
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

    /** The version check, annotated so that Android Lint sees the guard behind the injected [sdkInt] (QA #74). */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private fun isAtLeastTiramisu() = sdkInt() >= Build.VERSION_CODES.TIRAMISU
}
