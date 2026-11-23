package com.migrely.app.backup.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val backupScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Your data" to {
        MigrelyTheme {
            YourDataContents(
                working = false,
                automaticCopy = null,
                actions = YourDataActions({}, {}, {}, {}, {}),
                messages = {}
            )
        }
    }
)
