package com.migrely.app.datasource.appinfo.datasource

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private const val PACKAGE = "com.migrely.app"

class AppInfoDataSourceImplTest {
    private val packageManager: PackageManager = mock()

    private fun info(versionName: String?) = PackageInfo().apply { this.versionName = versionName }

    @Test
    fun `When asked then the installed version, on any Android version`() {
        given(packageManager.getPackageInfo(PACKAGE, 0)).willReturn(info("1.2.0"))
        given(
            packageManager.getPackageInfo(eq(PACKAGE), anyOrNull<PackageManager.PackageInfoFlags>())
        ).willReturn(info("1.3.0"))

        assertEquals(
            "1.2.0",
            AppInfoDataSourceImpl(packageManager, PACKAGE) { Build.VERSION_CODES.S }.versionName()
        )
        assertEquals(
            "1.3.0",
            AppInfoDataSourceImpl(packageManager, PACKAGE) {
                Build.VERSION_CODES.TIRAMISU
            }.versionName()
        )
    }

    @Test
    fun `Given Android does not say then an empty version`() {
        given(packageManager.getPackageInfo(PACKAGE, 0)).willReturn(info(null))
        assertEquals(
            "",
            AppInfoDataSourceImpl(packageManager, PACKAGE) { Build.VERSION_CODES.S }.versionName()
        )

        given(packageManager.getPackageInfo(PACKAGE, 0))
            .willThrow(PackageManager.NameNotFoundException())
        assertEquals(
            "",
            AppInfoDataSourceImpl(packageManager, PACKAGE) { Build.VERSION_CODES.S }.versionName()
        )
    }
}
