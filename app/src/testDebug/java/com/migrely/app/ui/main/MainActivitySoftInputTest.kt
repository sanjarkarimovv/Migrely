package com.migrely.app.ui.main

import android.app.Application
import android.content.ComponentName
import android.view.WindowManager
import com.migrely.app.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The window resizes for the keyboard instead of panning (BRD D180 (1), #83): Robolectric reads the merged manifest,
 * so a lost `windowSoftInputMode` on `MainActivity` fails here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MainActivitySoftInputTest {
    @Test
    fun `MainActivity resizes for the keyboard`() {
        val context = RuntimeEnvironment.getApplication()
        val info = context.packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0
        )

        assertEquals(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
            info.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        )
    }
}
