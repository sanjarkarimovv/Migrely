package com.migrely.app.designsystem.picker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
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
 * The full-screen range picker (#102, BRD D127). In Material's 360 dp dialog, at 200% font the
 * picker's header took all the height and the months got none.
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
        showScreen(LANDSCAPE, fontScale = 2f, start = OCTOBER_5, end = OCTOBER_13)

        val weeks = weeksInFull()
        assertTrue("only $weeks weeks show in full", weeks >= 2)
    }

    @Test
    fun atTwiceTheFontSizeInPortraitTwoWeeksShowInFull() {
        showScreen(PORTRAIT, fontScale = 2f, start = OCTOBER_5, end = OCTOBER_13)

        val weeks = weeksInFull()
        assertTrue("only $weeks weeks show in full", weeks >= 2)
    }

    @Test
    fun twoDaysAndDoneGiveTheRange() {
        showScreen()

        day(OCTOBER_3).performClick()
        day(OCTOBER_9).performClick()
        done().performClick()

        assertEquals(OCTOBER_3 to OCTOBER_9, confirmed)
    }

    @Test
    fun theSameDayTwiceGivesOneDay() {
        showScreen()

        day(OCTOBER_3).performClick()
        day(OCTOBER_3).performClick()
        done().performClick()

        assertEquals(OCTOBER_3 to OCTOBER_3, confirmed)
    }

    @Test
    fun doneIsOffUntilAnEndIsChosen() {
        showScreen()
        done().assertIsNotEnabled()

        day(OCTOBER_3).performClick()
        done().assertIsNotEnabled()

        day(OCTOBER_9).performClick()
        done().assertIsEnabled()
    }

    @Test
    fun cancelDismissesWithoutARange() {
        showScreen(start = OCTOBER_5, end = OCTOBER_13)

        composeTestRule.onNode(hasText(LABELS.cancel) and hasClickAction()).performClick()

        assertTrue(dismissed)
        assertNull(confirmed)
    }

    @Test
    fun theRangeGivenIsKeptByDone() {
        showPicker(start = OCTOBER_5, end = OCTOBER_13, lastDate = OCTOBER_13)

        done().performClick()

        assertEquals(OCTOBER_5 to OCTOBER_13, confirmed)
    }

    @Test
    fun aDayAfterTheLastDateCannotBePicked() {
        showPicker(start = OCTOBER_5, end = OCTOBER_13, lastDate = OCTOBER_13)

        day(OCTOBER_13.plusDays(1)).assertIsNotEnabled()
    }

    /** [DateRangePickerScreen] alone, in a box of [size] or filling the test's window. */
    private fun showScreen(
        size: DpSize? = null,
        fontScale: Float = 1f,
        start: LocalDate? = null,
        end: LocalDate? = null
    ) {
        composeTestRule.setContent {
            val state = remember {
                DateRangePickerState(
                    locale = LOCALE,
                    initialSelectedStartDateMillis = start?.utcMidnight(),
                    initialSelectedEndDateMillis = end?.utcMidnight(),
                    initialDisplayedMonthMillis = OCTOBER_5.withDayOfMonth(1).utcMidnight(),
                    yearRange = YEARS
                )
            }
            // Compose scales text linearly here; Android 14 and later scale large text less, so this
            // is stricter than a phone set to 200%.
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                InFixedLocale {
                    // A landscape phone is laid out at its own size on a test device held upright.
                    // The size is already less the system bars, so the test device's bars are not
                    // taken off again.
                    val box = size?.let {
                        Modifier
                            .requiredSize(it)
                            .consumeWindowInsets(WindowInsets.safeDrawing)
                    } ?: Modifier.fillMaxSize()
                    Box(box) {
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

    /** The whole [MigrelyDateRangePicker], in its own window. */
    private fun showPicker(start: LocalDate?, end: LocalDate?, lastDate: LocalDate) {
        composeTestRule.setContent {
            InFixedLocale {
                MigrelyDateRangePicker(
                    initialStart = start,
                    initialEnd = end,
                    lastDate = lastDate,
                    yearRange = YEARS,
                    labels = LABELS,
                    onConfirm = { first, last -> confirmed = first to last },
                    onDismiss = { dismissed = true }
                )
            }
        }
    }

    /** The test's locale, so the texts it looks for do not depend on the device's. */
    @Composable
    private fun InFixedLocale(content: @Composable () -> Unit) {
        val configuration = Configuration(LocalConfiguration.current).apply { setLocale(LOCALE) }
        CompositionLocalProvider(LocalConfiguration provides configuration) {
            MigrelyTheme(content = content)
        }
    }

    /** The week rows whose day cells show their whole touch target, not cut by the list's edge. */
    private fun weeksInFull(): Int {
        val density = composeTestRule.density
        return composeTestRule.onAllNodes(hasText(", 2026", substring = true) and hasClickAction())
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }
            .filter { bounds ->
                with(density) { bounds.height.toDp() } >= DAY_TOUCH_HEIGHT - TOLERANCE
            }
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
        val LANDSCAPE = DpSize(860.dp, 367.dp)
        val PORTRAIT = DpSize(411.dp, 840.dp)
        val DAY_TOUCH_HEIGHT = 48.dp
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
