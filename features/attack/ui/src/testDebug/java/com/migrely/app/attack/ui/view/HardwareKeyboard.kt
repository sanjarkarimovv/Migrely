package com.migrely.app.attack.ui.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration

/** [content] as on a device with a hardware keyboard attached when [attached], else as it is. */
@Composable
internal fun WithHardwareKeyboard(attached: Boolean, content: @Composable () -> Unit) {
    if (!attached) return content()
    val configuration = Configuration(LocalConfiguration.current).apply {
        hardKeyboardHidden = Configuration.HARDKEYBOARDHIDDEN_NO
    }
    CompositionLocalProvider(LocalConfiguration provides configuration, content = content)
}
