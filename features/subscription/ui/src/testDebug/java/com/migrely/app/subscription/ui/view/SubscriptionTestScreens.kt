package com.migrely.app.subscription.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.subscription.ui.model.PaywallViewStateUiModel
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val subscriptionScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Paywall" to {
        MigrelyTheme {
            PaywallContents(
                context = PaywallContextUiModel.Report,
                viewState = PaywallViewStateUiModel.Ready(PREVIEW_OFFERS),
                chosen = null,
                unlocked = null,
                actions = PaywallActions({}, {}, {}, {}, {}, {}, {}),
                messages = {}
            )
        }
    },
    "Today's trial banner" to {
        MigrelyTheme {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TrialBannerContents(
                    endsToday = true,
                    renews = true,
                    offlineNote = null,
                    onManage = {},
                    onDismiss = {}
                )
            }
        }
    }
)
