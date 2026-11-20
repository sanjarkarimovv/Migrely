package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.sky.CalendarDay
import com.migrely.app.designsystem.sky.DaySkyAppearance
import com.migrely.app.designsystem.sky.MonthSkyCalendar
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
/**
 * This month as Today shows it under its card (History's month card): November 2026, today the 20th, past days logged
 * as nothing, the days after today not yet selectable, as on the device walk that found #255's 6.2 jump.
 */
@Composable
private fun ThisMonth() {
    val weekdays =
        listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    // As History's month card draws it: a title that opens History, then the month.
    Column(Modifier.padding(top = 20.dp)) {
        Text(
            text = "November",
            modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics {
                    contentDescription = "November. Opens History."
                    heading()
                }
                .clickable(role = Role.Button, onClick = {})
        )
        MonthSkyCalendar(
            weekdayInitials = weekdays.map { it.take(1) },
            leadingEmptyDays = 0,
            days = (1..30).map { day -> thisMonthDay(day, weekdays[(day - 1) % 7]) },
            chosenStateDescription = "chosen",
            onDayClick = {},
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun thisMonthDay(day: Int, weekday: String) = CalendarDay(
    dayOfMonth = day,
    appearance = if (day <= TODAY) DaySkyAppearance.NothingLogged else DaySkyAppearance.Future,
    isToday = day == TODAY,
    isSelected = false,
    isSelectable = day <= TODAY,
    description = when {
        day == TODAY -> "Today, Friday, November 20: nothing logged."
        day < TODAY -> "$weekday, November $day: nothing logged."
        else -> "$weekday, November $day"
    }
)

private const val TODAY = 20

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
    "Today with this month's calendar" to {
        MigrelyTheme {
            TodayContents(
                today = TodayUiModel(SkyUiModel.Calm, 0, null),
                dayTitle = "Today",
                nowMillis = 0L,
                skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                timeUiFormatter = previewFormatter,
                onStartAttack = {},
                onAttackDetails = {},
                onAddPastAttack = {},
                monthCalendar = { ThisMonth() }
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
