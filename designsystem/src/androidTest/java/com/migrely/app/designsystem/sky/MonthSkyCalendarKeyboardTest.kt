package com.migrely.app.designsystem.sky

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The month as one Tab stop, with arrow keys between days (BRD D38, EC-UX-19). */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MonthSkyCalendarKeyboardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val clicked = mutableListOf<Int>()
    private val before = FocusRequester()
    private lateinit var inputModeManager: InputModeManager

    // October 2026 starts on a Thursday: three blank cells before it in a week starting on Monday.
    private fun show(
        today: Int?,
        chosen: Int? = null,
        lastSelectable: Int = today ?: 31,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr
    ) {
        composeTestRule.setContent {
            inputModeManager = LocalInputModeManager.current
            MigrelyTheme {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Column {
                        Button(onClick = {}, modifier = Modifier.focusRequester(before)) {
                            Text("Before")
                        }
                        MonthSkyCalendar(
                            weekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S"),
                            leadingEmptyDays = 3,
                            days = (1..31).map { number ->
                                CalendarDay(
                                    dayOfMonth = number,
                                    appearance = DaySkyAppearance.NothingLogged,
                                    isToday = number == today,
                                    isSelected = number == chosen,
                                    isSelectable = number <= lastSelectable,
                                    description = "Day $number"
                                )
                            },
                            chosenStateDescription = "Chosen",
                            onDayClick = { clicked += it },
                            modifier = Modifier.width(350.dp)
                        )
                        Button(onClick = {}) { Text("After") }
                    }
                }
            }
        }
        composeTestRule.runOnIdle {
            inputModeManager.requestInputMode(InputMode.Keyboard)
            before.requestFocus()
        }
    }

    private fun press(key: Key) = composeTestRule.onRoot().performKeyInput { pressKey(key) }

    private fun pressShiftTab() = composeTestRule.onRoot().performKeyInput {
        withKeyDown(Key.ShiftLeft) { pressKey(Key.Tab) }
    }

    private fun assertDayFocused(number: Int) {
        composeTestRule.onNodeWithContentDescription("Day $number").assertIsFocused()
    }

    @Test
    fun tabEntersOnTodayAndTheNextTabLeavesTheMonth() {
        show(today = 23)

        press(Key.Tab)
        assertDayFocused(23)

        press(Key.Tab)
        composeTestRule.onNodeWithText("After").assertIsFocused()
    }

    @Test
    fun shiftTabEntersOnTodayAndLeavesTheMonthBackwards() {
        show(today = 23)
        press(Key.Tab)
        press(Key.Tab)

        pressShiftTab()
        assertDayFocused(23)

        pressShiftTab()
        composeTestRule.onNodeWithText("Before").assertIsFocused()
    }

    @Test
    fun tabEntersOnTheChosenDay() {
        show(today = 23, chosen = 9)

        press(Key.Tab)

        assertDayFocused(9)
    }

    @Test
    fun tabEntersOnTheFirstDayOfAPastMonthAndLeavesIt() {
        show(today = null)

        press(Key.Tab)
        assertDayFocused(1)

        press(Key.Tab)
        composeTestRule.onNodeWithText("After").assertIsFocused()
    }

    @Test
    fun arrowKeysMoveBetweenDays() {
        show(today = 23, chosen = 14)
        press(Key.Tab)

        press(Key.DirectionRight)
        assertDayFocused(15)

        press(Key.DirectionDown)
        assertDayFocused(22)
    }

    @Test
    fun enterChoosesTheFocusedDay() {
        show(today = 23)
        press(Key.Tab)
        press(Key.DirectionLeft)

        press(Key.Enter)

        composeTestRule.runOnIdle { assertEquals(listOf(22), clicked) }
    }

    @Test
    fun anArrowOffTheSelectableDaysKeepsFocus() {
        show(today = 24)
        press(Key.Tab)

        // The 25th is not selectable yet, and no other day is to the right of today in its week.
        press(Key.DirectionRight)
        assertDayFocused(24)

        press(Key.DirectionDown)
        assertDayFocused(24)
    }

    @Test
    fun rightToLeftMirrorsTheArrows() {
        show(today = 23, chosen = 14, layoutDirection = LayoutDirection.Rtl)
        press(Key.Tab)

        press(Key.DirectionLeft)

        assertDayFocused(15)
    }

    @Test
    fun aChosenDayThatCannotBeChosenIsSkipped() {
        show(today = 10, chosen = 20)

        press(Key.Tab)

        assertDayFocused(10)
    }

    @Test
    fun aMonthWithNoSelectableDayIsPassedOver() {
        show(today = null, lastSelectable = 0)

        press(Key.Tab)

        composeTestRule.onNodeWithText("After").assertIsFocused()
    }
}
