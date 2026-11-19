package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import com.migrely.app.test.talkback.TOUCH_BAR_DP
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val homeScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Today" to {
        MigrelyTheme {
            TodayContents(
                today = TodayUiModel(SkyUiModel.Calm, 0, null),
                dayTitle = "Today",
                nowMillis = 0L,
                skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                timeUiFormatter = previewFormatter,
                onStartAttack = {},
                onAttackDetails = {},
                onAddPastAttack = {}
            )
        }
    },
    "Attack mode" to {
        MigrelyTheme {
            AttackModeContents(
                attack = AttackInProgressUiModel(
                    "a",
                    0L,
                    "Asia/Tashkent",
                    IntensityLevelUiModel.Moderate,
                    false
                ),
                nowMillis = 0L,
                reduceMotion = true,
                messageBottom = { 0.dp },
                timeUiFormatter = previewFormatter,
                actions = previewActions
            )
        }
    }
)
