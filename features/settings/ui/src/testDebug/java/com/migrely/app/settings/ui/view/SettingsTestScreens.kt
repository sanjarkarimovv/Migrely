package com.migrely.app.settings.ui.view

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
internal val settingsScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Settings" to {
        MigrelyTheme {
            SettingsContents(
                settings = PREVIEW_SETTINGS,
                actions = SettingsActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
                messages = {}
            )
        }
    },
    "Warning signs" to {
        MigrelyTheme {
            WarningSignsContents(onBack = {})
        }
    },
    "About Migrely" to {
        MigrelyTheme {
            AboutMigrelyContents(onBack = {})
        }
    },
    "Privacy" to {
        MigrelyTheme {
            PrivacyContents(onBack = {})
        }
    },
    "Licences" to {
        MigrelyTheme {
            LicencesContents(onBack = {})
        }
    },
    "Delete all data" to {
        MigrelyTheme {
            DeleteAllDataContents(
                typed = "",
                word = "DELETE",
                confirmed = false,
                deleting = false,
                failed = false,
                actions = DeleteAllDataActions({}, {}, {})
            )
        }
    }
)
