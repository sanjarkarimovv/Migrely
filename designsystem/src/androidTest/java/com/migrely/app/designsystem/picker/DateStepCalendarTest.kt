package com.migrely.app.designsystem.picker

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The date step's calendar in a box as short as the date picker dialog gets in landscape (QA #101).
 * Material's month grid has a fixed height, so without scrolling the first week spilled over the
 * weekday letters and the last one under the dialog's buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class DateStepCalendarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var state: DatePickerState

    @Before
    fun setUp() {
        composeTestRule.setContent {
            state = rememberDatePickerState(initialSelectedDateMillis = SEPTEMBER_15.utcMidnight())
            MigrelyTheme {
                Box(Modifier.size(width = 360.dp, height = SHORT_HEIGHT).testTag(BOX)) {
                    DateStepCalendar(state = state, title = "When did it start?")
                }
            }
        }
    }

    @Test
    fun everyDayOfTheMonthCanBeScrolledIntoViewBelowTheWeekdayLetters() {
        val box = composeTestRule.onNodeWithTag(BOX).getUnclippedBoundsInRoot()
        for (dayOfMonth in 1..SEPTEMBER_15.lengthOfMonth()) {
            val date = SEPTEMBER_15.withDayOfMonth(dayOfMonth)
            val day = dayNode(date)
            scrollIntoView(day)

            val bounds = day.getUnclippedBoundsInRoot()
            val weekdays = composeTestRule.onNodeWithContentDescription(MONDAY)
                .getUnclippedBoundsInRoot()
            assertTrue(
                "$date is drawn over the weekday letters",
                bounds.top >= weekdays.bottom - TOLERANCE
            )
            assertTrue("$date starts above the box", bounds.top >= box.top - TOLERANCE)
            assertTrue("$date ends below the box", bounds.bottom <= box.bottom + TOLERANCE)
        }
    }

    @Test
    fun theFirstDayCanBePicked() {
        val day = dayNode(SEPTEMBER_15.withDayOfMonth(1))
        scrollIntoView(day)

        day.performClick()

        composeTestRule.runOnIdle {
            assertEquals(SEPTEMBER_15.withDayOfMonth(1).utcMidnight(), state.selectedDateMillis)
        }
    }

    @Test
    fun aYearCanBePickedFromTheYearList() {
        composeTestRule.onNode(hasText(monthYear(SEPTEMBER_15)) and hasClickAction()).performClick()
        // Material describes a year cell as "Navigate to year 2025".
        composeTestRule.onNode(
            hasText("2025", substring = true) and hasClickAction()
        ).performClick()

        composeTestRule.onNode(
            hasText(monthYear(SEPTEMBER_15.minusYears(1))) and hasClickAction()
        ).assertExists()
    }

    private fun dayNode(date: LocalDate): SemanticsNodeInteraction =
        composeTestRule.onNode(hasText(fullDate(date), substring = true) and hasClickAction())

    /** Scrolls the calendar, if it scrolls vertically, until [node] is inside the box. */
    private fun scrollIntoView(node: SemanticsNodeInteraction) {
        val scrollables = composeTestRule.onAllNodes(hasScrollAction() and VERTICAL)
        if (scrollables.fetchSemanticsNodes().isEmpty()) return
        val box = composeTestRule.onNodeWithTag(BOX).getUnclippedBoundsInRoot()
        val target = node.getUnclippedBoundsInRoot()
        val delta: Dp = when {
            target.top < box.top -> target.top - box.top
            target.bottom > box.bottom -> target.bottom - box.bottom
            else -> 0.dp
        }
        if (delta == 0.dp) return
        val pixels = with(composeTestRule.density) { delta.toPx() }
        scrollables.onFirst().performSemanticsAction(SemanticsActions.ScrollBy) { scrollBy ->
            scrollBy(0f, pixels)
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        const val BOX = "box"
        val SHORT_HEIGHT = 250.dp
        val TOLERANCE = 0.5.dp
        val SEPTEMBER_15: LocalDate = LocalDate.of(2026, 9, 15)
        val VERTICAL = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        val MONDAY: String = DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, Locale.getDefault())

        fun LocalDate.utcMidnight(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        /** The date as Material's day cells describe it, e.g. "Tuesday, September 1, 2026". */
        fun fullDate(date: LocalDate): String = withSkeleton(date, "yMMMMEEEEd")

        /** The month as Material's year button shows it, e.g. "September 2026". */
        fun monthYear(date: LocalDate): String = withSkeleton(date, "yMMMM")

        fun withSkeleton(date: LocalDate, skeleton: String): String {
            val locale = Locale.getDefault()
            return DateTimeFormatter.ofPattern(
                DateFormat.getBestDateTimePattern(locale, skeleton),
                locale
            ).format(date)
        }
    }
}
