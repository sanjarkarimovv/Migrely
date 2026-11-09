package com.migrely.app.designsystem.picker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
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

/** The full-screen day picker (BRD D127, D168, design D9). */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class DatePickerScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var confirmed: LocalDate? = null
    private var dismissed = false

    @Test
    fun atTwiceTheFontSizeInLandscapeTheWeekdaysTwoWeeksAndDoneShow() {
        showScreen(LANDSCAPE, fontScale = 2f, picked = NOVEMBER_20)

        val weeks = weeksInFull()
        assertTrue("only $weeks weeks show in full", weeks >= 2)
        assertTrue("the weekday row is not in view", weekdayRowInView())
        // Within the box's height; the box is wider than the upright test window, so only the walk sees it whole.
        val done = done().fetchSemanticsNode().boundsInRoot
        val screenBottom = with(composeTestRule.density) { LANDSCAPE.height.toPx() }
        assertTrue("Done is at $done", done.top >= 0f && done.bottom <= screenBottom)
    }

    @Test
    fun doneIsOffUntilADayIsPicked_andTheHeadlineSaysSo() {
        showScreen()

        done().assertIsNotEnabled()
        composeTestRule.onNode(hasText(LABELS.none)).assertIsDisplayed()

        day(NOVEMBER_20).performClick()

        done().assertIsEnabled()
        composeTestRule.onNode(hasContentDescription("Friday, November 20, 2026")).assertExists()
        composeTestRule.onNode(hasLivePoliteRegion() and hasText("Fri, Nov 20")).assertExists()
    }

    @Test
    fun aPickedDayAndDoneGiveThatDay() {
        showScreen()

        day(NOVEMBER_20).performClick()
        done().performClick()

        assertEquals(NOVEMBER_20, confirmed)
    }

    @Test
    fun daysBeforeTodayCannotBePicked() {
        showScreen()

        day(TODAY.minusDays(1)).assertIsNotEnabled()
        day(TODAY).assertIsEnabled()
    }

    @Test
    fun cancelDismissesWithoutADay() {
        showScreen(picked = NOVEMBER_20)

        composeTestRule.onNode(hasText(LABELS.cancel) and hasClickAction()).performClick()

        assertTrue(dismissed)
        assertNull(confirmed)
    }

    private fun hasLivePoliteRegion() =
        SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

    /** [DatePickerScreen] alone, in a box of [size] or filling the test's window. */
    private fun showScreen(size: DpSize? = null, fontScale: Float = 1f, picked: LocalDate? = null) {
        composeTestRule.setContent {
            val state = remember {
                DatePickerState(
                    locale = LOCALE,
                    initialSelectedDateMillis = picked?.utcMidnight(),
                    initialDisplayedMonthMillis = TODAY.withDayOfMonth(1).utcMidnight(),
                    yearRange = 2026..2027,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long) =
                            utcTimeMillis >= TODAY.utcMidnight()
                    }
                )
            }
            // Compose scales text linearly here; Android 14 and later scale large text less, so this is stricter than
            // a phone set to 200%.
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                InFixedLocale {
                    val box = size?.let {
                        Modifier
                            .requiredSize(it)
                            .consumeWindowInsets(WindowInsets.safeDrawing)
                    } ?: Modifier.fillMaxSize()
                    Box(box) {
                        DatePickerScreen(
                            state = state,
                            labels = LABELS,
                            today = TODAY,
                            onConfirm = { confirmed = it },
                            onDismiss = { dismissed = true }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun InFixedLocale(content: @Composable () -> Unit) {
        val configuration = Configuration(LocalConfiguration.current).apply { setLocale(LOCALE) }
        CompositionLocalProvider(LocalConfiguration provides configuration) {
            MigrelyTheme(content = content)
        }
    }

    /** The week rows whose day cells show their whole touch target, not cut by the screen's edge. */
    private fun weeksInFull(): Int {
        val density = composeTestRule.density
        val screenBottom = with(density) { LANDSCAPE.height.toPx() }
        return composeTestRule.onAllNodes(hasText(", 2026", substring = true) and hasClickAction())
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }
            .filter { bounds ->
                with(density) { bounds.height.toDp() } >= DAY_TOUCH_HEIGHT - TOLERANCE &&
                    bounds.bottom <= screenBottom
            }
            .map { it.top }
            .distinct()
            .size
    }

    /**
     * The weekday row's letter for Monday lies within the screen's height. Only the height counts: the 860 dp box is
     * centred in a test window held upright, and the month pager keeps its other pages beside the shown one.
     */
    private fun weekdayRowInView(): Boolean {
        val screenBottom = with(composeTestRule.density) { LANDSCAPE.height.toPx() }
        return composeTestRule.onAllNodes(
            hasText("M"),
            useUnmergedTree = true
        ).fetchSemanticsNodes()
            .any { it.boundsInRoot.top >= 0f && it.boundsInRoot.bottom <= screenBottom }
    }

    private fun day(date: LocalDate) =
        composeTestRule.onNode(hasText(fullDate(date), substring = true) and hasClickAction())

    private fun done() = composeTestRule.onNode(hasText(LABELS.confirm) and hasClickAction())

    private companion object {
        val LABELS = DatePickerLabels(
            title = "Next visit",
            confirm = "Done",
            cancel = "Cancel",
            none = "Pick a day"
        )

        /** A 1080×2400 phone at 420 dpi in landscape, less its bars: about 411 dp minus the status bar. */
        val LANDSCAPE = DpSize(860.dp, 367.dp)
        val DAY_TOUCH_HEIGHT = 48.dp
        val TOLERANCE = 0.5.dp
        val TODAY: LocalDate = LocalDate.of(2026, 11, 10)
        val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
        val LOCALE: Locale = Locale.US
        val FORMATTER = DatePickerDefaults.dateFormatter()

        fun fullDate(date: LocalDate): String = checkNotNull(
            FORMATTER.formatDate(date.utcMidnight(), LOCALE, forContentDescription = true)
        )
    }
}
