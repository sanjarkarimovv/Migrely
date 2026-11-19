package com.migrely.app.designsystem.sky

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.PageScroll
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val CALENDAR = "calendar"
private const val DAYS = 30
private const val WEEK = 7
private const val GAPS_IN_A_ROW = 6
private const val GAP_DP = 6

/**
 * A day's touch area is the whole cell pitch, the cell and half the gap on each side (#255, design D5 T1; lead decision
 * 2026-11-19, option 1): seven 48 dp targets do not fit a 328 dp row, and inside a page's exact bounds Compose no
 * longer widens a cell over the gaps. Nothing drawn moves.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MonthSkyCalendarPitchTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val clicked = mutableListOf<Int>()

    private fun show(fontScale: Float = 1f) = rule.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            MigrelyTheme {
                PageScroll {
                    Column(Modifier.pageScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                        MonthSkyCalendar(
                            weekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S"),
                            leadingEmptyDays = 0,
                            days = (1..DAYS).map { day ->
                                CalendarDay(
                                    dayOfMonth = day,
                                    appearance = if (day % 3 == 0) {
                                        DaySkyAppearance.entries.last()
                                    } else {
                                        DaySkyAppearance.NothingLogged
                                    },
                                    isToday = day == 10,
                                    isSelected = day == 5,
                                    isSelectable = true,
                                    description = "Day $day",
                                    hasPeriodMark = day == 12
                                )
                            },
                            chosenStateDescription = "chosen",
                            onDayClick = { clicked += it },
                            modifier = Modifier.testTag(CALENDAR)
                        )
                    }
                }
            }
        }
    }

    /** Taps at [dx] px right of the calendar's left edge and [y] px down the window. */
    private fun tap(dx: Float, y: Float) {
        val calendar = rule.onNodeWithTag(CALENDAR).fetchSemanticsNode().boundsInRoot
        rule.onRoot().performTouchInput { click(Offset(calendar.left + dx, y)) }
        rule.waitForIdle()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `At 360 dp a tap in the gap between two days opens the nearer one`() {
        show()
        val gap = with(rule.density) { GAP_DP.dp.toPx() }
        val oneDp = with(rule.density) { 1.dp.toPx() }
        val width = rule.onNodeWithTag(CALENDAR).fetchSemanticsNode().size.width
        val cell = (width - GAPS_IN_A_ROW * gap) / WEEK
        // Day 1's drawn centre: the cell's own middle, the same however its touch area is laid out.
        val firstRow = rule.onNodeWithContentDescription(
            "Day 1"
        ).fetchSemanticsNode().boundsInRoot.center.y
        tap(cell + oneDp, firstRow)
        tap(cell + gap - oneDp, firstRow)
        val firstColumn = cell / 2
        val below = firstRow + cell / 2
        rule.onRoot().performTouchInput {
            val left = rule.onNodeWithTag(CALENDAR).fetchSemanticsNode().boundsInRoot.left
            click(Offset(left + firstColumn, below + oneDp))
        }
        rule.waitForIdle()
        rule.onRoot().performTouchInput {
            val left = rule.onNodeWithTag(CALENDAR).fetchSemanticsNode().boundsInRoot.left
            click(Offset(left + firstColumn, below + gap - oneDp))
        }
        rule.waitForIdle()
        assertEquals(listOf(1, 2, 1, 1 + WEEK), clicked)
    }

    /**
     * The window's pixels against [golden], rendered from the calendar as it was before its days took their pitch
     * (develop's code, 0fd87fc0's parent) and kept as a test resource: nothing drawn may move. Returns how many differ.
     */
    private fun differingFromGolden(golden: String): Int {
        val decor = rule.activity.window.decorView
        val window = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { decor.draw(Canvas(window)) }
        val expected = javaClass.getResourceAsStream("/calendar/$golden.png").use {
            BitmapFactory.decodeStream(it)
        }
        assertEquals("size", expected.width to expected.height, window.width to window.height)
        val now = IntArray(window.width * window.height)
        val before = IntArray(now.size)
        window.getPixels(now, 0, window.width, 0, 0, window.width, window.height)
        expected.getPixels(before, 0, window.width, 0, 0, window.width, window.height)
        return now.indices.count { now[it] != before[it] }
    }

    private fun showAndCompare(golden: String, fontScale: Float) {
        show(fontScale)
        // The keyboard's focus ring, two-tone, on day 3.
        rule.onNodeWithContentDescription(
            "Day 3"
        ).performSemanticsAction(SemanticsActions.RequestFocus)
        rule.waitForIdle()
        assertEquals("pixels that differ from $golden", 0, differingFromGolden(golden))
    }

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `Pixels at 360 dp, 100 percent`() = showAndCompare("360-100", 1f)

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `Pixels at 360 dp, 200 percent`() = showAndCompare("360-200", 2f)

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Pixels at 411 dp, 100 percent`() = showAndCompare("411-100", 1f)

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Pixels at 411 dp, 200 percent`() = showAndCompare("411-200", 2f)
}
