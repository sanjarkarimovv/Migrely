package com.migrely.app.reminders.ui.view

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import com.migrely.app.test.talkback.auditTouchTargets
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val TODAY = LocalDate.of(2026, 11, 19)

private val VISIT = TODAY.plusDays(3)

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val remindersScreens: Map<String, @Composable () -> Unit> = mapOf(
    "Reminders" to {
        MigrelyTheme {
            RemindersContents(
                viewState = RemindersViewStateUiModel.Ready(
                    RemindersUiModel(
                        dailyEnabled = true,
                        dailyMinuteOfDay = 1200,
                        effectEnabled = true,
                        notificationsAllowed = true,
                        mayAskPermission = true,
                        todayEpochDay = TODAY.toEpochDay(),
                        nextVisitEpochDay = VISIT.toEpochDay(),
                        visitRemovable = true,
                        visitChannelOff = false
                    )
                ),
                actions = RemindersActions({}, {}, {}, {}, {}, {}),
                messages = {}
            )
        }
    },
    "Today's visit card" to {
        MigrelyTheme {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                VisitCardContents(VISIT, onReport = {}, onClose = {})
            }
        }
    }
)
