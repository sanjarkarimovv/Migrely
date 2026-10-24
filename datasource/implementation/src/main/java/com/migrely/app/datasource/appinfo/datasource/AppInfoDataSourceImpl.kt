package com.migrely.app.datasource.appinfo.datasource

import android.content.pm.PackageManager
import android.os.Build

/** Reads the installed version of [packageName], the app's own, from Android's package manager. */
class AppInfoDataSourceImpl(
    private val packageManager: PackageManager,
    private val packageName: String,
    private val sdkInt: () -> Int = { Build.VERSION.SDK_INT }
) : AppInfoDataSource {
    @Suppress("SwallowedException")
    override fun versionName(): String = try {
        val info = if (sdkInt() >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        info.versionName.orEmpty()
    } catch (exception: PackageManager.NameNotFoundException) {
        ""
    }
}
