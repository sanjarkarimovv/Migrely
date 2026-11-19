package com.migrely.app.history.ui.view

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import com.migrely.app.test.talkback.TOUCH_BAR_DP
import com.migrely.app.test.talkback.auditTouchTargets
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ZONE = "Asia/Tashkent"

private val CHOSEN = LocalDate.of(2026, 10, 10)

/** The preview month, with a migraine on the chosen day, so its list shows a row. */
private fun month() = previewMonth().let { preview ->
    val start = CHOSEN.atTime(9, 0).atZone(ZoneId.of(ZONE)).toInstant().toEpochMilli()
    val attack = HistoryAttackUiModel("a", start, ZONE, start + 3_600_000L, 6)
    preview.copy(
        days = preview.days.map {
            if (it.epochDay == CHOSEN.toEpochDay()) it.copy(attacks = listOf(attack)) else it
        }
    )
}

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val historyScreens: Map<String, @Composable () -> Unit> = mapOf(
    "History" to {
        MigrelyTheme {
            HistoryContents(
                viewState = HistoryViewStateUiModel.Ready(month(), null),
                selectedEpochDay = CHOSEN.toEpochDay(),
                mappers = previewMappers,
                actions = previewActions,
                messages = {},
                overuseIndicator = {}
            )
        }
    }
)
