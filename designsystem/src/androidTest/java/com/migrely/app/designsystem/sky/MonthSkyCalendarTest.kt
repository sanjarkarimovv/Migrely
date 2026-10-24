package com.migrely.app.designsystem.sky

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MonthSkyCalendarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val clicked = mutableListOf<Int>()

    private fun day(number: Int, appearance: DaySkyAppearance, selected: Boolean = false) =
        CalendarDay(
            dayOfMonth = number,
            appearance = appearance,
            isToday = number == 3,
            isSelected = selected,
            description = "Day $number"
        )

    @Before
    fun setUp() {
        composeTestRule.setContent {
            MigrelyTheme {
                MonthSkyCalendar(
                    weekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S"),
                    leadingEmptyDays = 3,
                    days = listOf(
                        day(1, DaySkyAppearance.Storm, selected = true),
                        day(2, DaySkyAppearance.NothingLogged),
                        day(3, DaySkyAppearance.Rain),
                        day(4, DaySkyAppearance.Future)
                    ),
                    chosenStateDescription = "Chosen",
                    onDayClick = { clicked += it },
                    modifier = Modifier.width(350.dp)
                )
            }
        }
    }

    @Test
    fun everyDayIsReadByItsDescription() {
        (1..4).forEach { number ->
            composeTestRule.onNodeWithContentDescription("Day $number").assertExists()
        }
    }

    @Test
    fun aPastDayIsSelectableAndReportsItsNumber() {
        composeTestRule.onNodeWithContentDescription("Day 2").performClick()

        assertEquals(listOf(2), clicked)
    }

    @Test
    fun aFutureDayHasNoClickAction() {
        composeTestRule.onNodeWithContentDescription("Day 4")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    @Test
    fun onlyTheChosenDaySaysSoAndNoDayIsACheckbox() {
        composeTestRule.onNodeWithContentDescription("Day 1")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Chosen"))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
        composeTestRule.onNodeWithContentDescription("Day 2")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }

    @Test
    fun leadingEmptyDaysPushTheFirstDayToItsWeekday() {
        val first = composeTestRule.onNodeWithContentDescription("Day 1").getUnclippedBoundsInRoot()
        val fourth = composeTestRule.onNodeWithContentDescription(
            "Day 4"
        ).getUnclippedBoundsInRoot()

        // Day 1 is the fourth cell of the first week and day 4 the seventh; both sit on the same row.
        assertEquals(first.top, fourth.top)
        assertTrue(first.left > 350.dp * 3 / 7 - 6.dp)
    }
}
