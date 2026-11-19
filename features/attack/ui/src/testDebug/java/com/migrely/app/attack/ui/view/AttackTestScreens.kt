package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.PastAttackDraftUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NOW = 1_792_829_200_000L

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val attackScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Attack details" to @Composable {
        MigrelyTheme {
            AttackDetailsContents(
                viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                timeUiFormatter = previewTimeFormatter,
                labels = AttackLabels.Default,
                localState = AttackDetailsLocalState(
                    timeError = null,
                    safetyCardVisible = true,
                    onDismissSafetyCard = {},
                    onTimeErrorShown = {}
                ),
                actions = previewActions
            )
        }
    },
    "Add past attack" to @Composable {
        MigrelyTheme {
            AddPastAttackContents(
                draft = PastAttackDraftUiModel(NOW - 7_200_000L, NOW, "UTC"),
                problem = null,
                saving = false,
                timeUiFormatter = previewTimeFormatter,
                actions = AddPastAttackActions({}, {}, {}, {}, {}, {}),
                messages = {}
            )
        }
    }
)
