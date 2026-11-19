package com.migrely.app.report.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.report.ui.model.ReportOptionsUiModel
import com.migrely.app.report.ui.model.ReportPeriodUiModel
import com.migrely.app.report.ui.model.ReportViewStateUiModel
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val reportScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Report" to {
        MigrelyTheme {
            ReportContents(
                viewState = ReportViewStateUiModel.Ready(
                    ReportOptionsUiModel(20_660, 20_751, anythingLogged = true)
                ),
                period = ReportPeriodUiModel.Default,
                includeNotes = false,
                actions = ReportActions({}, {}, {}, {}, {}),
                messages = {}
            )
        }
    }
)
