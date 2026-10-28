package com.migrely.app.designsystem.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The full-screen range picker (#102, BRD D127). With Material's own title and headline, at 200%
 * font the header filled the picker and the months got no height.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class DateRangePickerScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var confirmed: Pair<LocalDate, LocalDate>? = null
    private var dismissed = false

    @Test
    fun atTwiceTheFontSizeInLandscapeTwoWeeksShowInFull() {
        show(LANDSCAPE_WIDTH, LANDSCAPE_HEIGHT, fontScale = 2f, start = OCTOBER_5, end = OCTOBER_13)

        val weeks = weeksInFull()
        assertTrue("only $weeks weeks show in full", weeks >= 2)
    }

    @Test
    fun atTwiceTheFontSizeInPortraitTwoWeeksShowInFull() {
        show(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, fontScale = 2f, start = OCTOBER_5, end = OCTOBER_13)

        val weeks = weeksInFull()
        assertTrue("only $weeks weeks show in full", weeks >= 2)
    }

    @Test
    fun twoDaysAndDoneGiveTheRange() {
        show(PORTRAIT_WIDTH, PORTRAIT_HEIGHT)

        day(OCTOBER_3).performClick()
        day(OCTOBER_9).performClick()
        done().performClick()

        assertEquals(OCTOBER_3 to OCTOBER_9, confirmed)
    }

    @Test
    fun aStartAloneAndDoneGiveOneDay() {
        show(PORTRAIT_WIDTH, PORTRAIT_HEIGHT)

        day(OCTOBER_3).performClick()
        done().performClick()

        assertEquals(OCTOBER_3 to OCTOBER_3, confirmed)
    }

    @Test
    fun doneIsOffUntilAStartIsChosen() {
        show(PORTRAIT_WIDTH, PORTRAIT_HEIGHT)

        done().assertIsNotEnabled()
    }

    @Test
    fun cancelDismissesWithoutARange() {
        show(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, start = OCTOBER_5, end = OCTOBER_13)

        composeTestRule.onNode(hasText(LABELS.cancel) and hasClickAction()).performClick()

        assertTrue(dismissed)
        assertNull(confirmed)
    }

    private fun show(
        width: Dp,
        height: Dp,
        fontScale: Float = 1f,
        start: LocalDate? = null,
        end: LocalDate? = null
    ) {
        composeTestRule.setContent {
            // A fixed locale, so the texts the test looks for do not depend on the device's.
            val state = remember {
                DateRangePickerState(
                    locale = LOCALE,
                    initialSelectedStartDateMillis = start?.utcMidnight(),
                    initialSelectedEndDateMillis = end?.utcMidnight(),
                    initialDisplayedMonthMillis = OCTOBER_5.withDayOfMonth(1).utcMidnight(),
                    yearRange = YEARS
                )
            }
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MigrelyTheme {
                    // Required, so a landscape phone can be laid out on a test device held
                    // upright. The size is already less the system bars, so the test device's
                    // own bars are not taken off again.
                    Box(
                        Modifier
                            .requiredSize(width, height)
                            .consumeWindowInsets(WindowInsets.safeDrawing)
                    ) {
                        DateRangePickerScreen(
                            state = state,
                            labels = LABELS,
                            onConfirm = { first, last -> confirmed = first to last },
                            onDismiss = { dismissed = true }
                        )
                    }
                }
            }
        }
    }

    /** The week rows whose day cells show at full height; a cell past the device's edge is none. */
    private fun weeksInFull(): Int {
        val density = composeTestRule.density
        return composeTestRule.onAllNodes(hasText(", 2026", substring = true) and hasClickAction())
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }
            .filter { bounds -> with(density) { bounds.height.toDp() } >= DAY_HEIGHT - TOLERANCE }
            .map { it.top }
            .distinct()
            .size
    }

    private fun day(date: LocalDate) =
        composeTestRule.onNode(hasText(fullDate(date), substring = true) and hasClickAction())

    private fun done() = composeTestRule.onNode(hasText(LABELS.confirm) and hasClickAction())

    private companion object {
        val LABELS = DateRangePickerLabels(
            title = "Choose the dates",
            confirm = "Done",
            cancel = "Cancel"
        )

        /** A 1080×2400 phone at 420 dpi, less its status and navigation bars. */
        val LANDSCAPE_WIDTH = 860.dp
        val LANDSCAPE_HEIGHT = 367.dp
        val PORTRAIT_WIDTH = 411.dp
        val PORTRAIT_HEIGHT = 840.dp
        val DAY_HEIGHT = 40.dp
        val TOLERANCE = 0.5.dp
        val YEARS = 2024..2026
        val OCTOBER_3: LocalDate = LocalDate.of(2026, 10, 3)
        val OCTOBER_5: LocalDate = LocalDate.of(2026, 10, 5)
        val OCTOBER_9: LocalDate = LocalDate.of(2026, 10, 9)
        val OCTOBER_13: LocalDate = LocalDate.of(2026, 10, 13)
        val LOCALE: Locale = Locale.US
        val FORMATTER = DatePickerDefaults.dateFormatter()

        /** The date as Material's day cells describe it: "Saturday, October 3, 2026". */
        fun fullDate(date: LocalDate): String = checkNotNull(
            FORMATTER.formatDate(date.utcMidnight(), LOCALE, forContentDescription = true)
        )
    }
}
