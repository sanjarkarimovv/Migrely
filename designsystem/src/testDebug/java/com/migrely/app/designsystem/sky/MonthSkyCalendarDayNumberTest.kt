package com.migrely.app.designsystem.sky

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.ShownTextLayoutsKey
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val CALENDAR = "calendar"
private const val DAYS = 31
private const val TODAY = 23
private const val CHOSEN = 16
private const val WEEK = 7
private const val GAPS_IN_A_ROW = 6
private val GAP = 6.dp

// The number's room in its cell: 5 dp at the start, 3 dp at the end (BRD D198).
private val NUMBER_INSETS = 8.dp

// The calendar's width in History at 640 x 360 dp, 200% (R25: x 150 to 975 px at 3x), so a cell of about 34 dp.
private val LANDSCAPE_CALENDAR = 275.dp

// Narrower than a cell the 100% number fits (20.2 dp): a 14.9 dp cell, as in a split screen before the pane guard.
private val TOO_NARROW_CALENDAR = 140.dp

private val ENGLISH_INITIALS = listOf("M", "T", "W", "T", "F", "S", "S")
private val GERMAN_INITIALS = listOf("M", "D", "M", "D", "F", "S", "S")

/**
 * A day number is one line that is never broken or cut, and all numbers of the grid share one size: the person's when
 * the widest two-digit day fits its cell, else the largest that fits, never below the 100% size (#275, BRD D198,
 * EC-HIS-05). The weekday initials follow the same rule. Before the fix, every two-digit day wrapped at 200% in
 * landscape and the cell cut its second digit, so 16 read "1".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MonthSkyCalendarDayNumberTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var labelSmall: TextStyle

    private fun show(
        fontScale: Float,
        calendarWidth: Dp?,
        initials: List<String> = ENGLISH_INITIALS
    ) {
        RuntimeEnvironment.setFontScale(fontScale)
        rule.setContent {
            MigrelyTheme {
                labelSmall = MaterialTheme.typography.labelSmall
                Box(
                    if (calendarWidth ==
                        null
                    ) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(calendarWidth)
                    }
                ) {
                    MonthSkyCalendar(
                        weekdayInitials = initials,
                        leadingEmptyDays = 3,
                        days = (1..DAYS).map { day ->
                            CalendarDay(
                                dayOfMonth = day,
                                appearance = when {
                                    day > TODAY -> DaySkyAppearance.Future
                                    day % 3 == 0 -> DaySkyAppearance.Storm
                                    else -> DaySkyAppearance.NothingLogged
                                },
                                isToday = day == TODAY,
                                isSelected = day == CHOSEN,
                                isSelectable = day <= TODAY,
                                description = "Day $day",
                                hasPeriodMark = day == 12
                            )
                        },
                        chosenStateDescription = "chosen",
                        onDayClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(CALENDAR)
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun shownLayouts(): List<List<TextLayoutResult>> =
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(ShownTextLayoutsKey), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .map { it.config[ShownTextLayoutsKey].all }

    private fun dayNumbers(): List<TextLayoutResult> = shownLayouts().flatten()
        .filter { layout -> layout.layoutInput.text.text.all(Char::isDigit) }
        .sortedBy { it.layoutInput.text.text.toInt() }

    private fun initials(): List<TextLayoutResult> = shownLayouts().single { it.size == WEEK }

    /** A drawn cell's width in px: the calendar less its six gaps, over seven. */
    private fun cellPx(): Float {
        val width = rule.onNodeWithTag(CALENDAR).fetchSemanticsNode().size.width
        return (width - GAPS_IN_A_ROW * with(rule.density) { GAP.toPx() }) / WEEK
    }

    private fun TextLayoutResult.lineWidth() = getLineRight(0) - getLineLeft(0)

    private fun TextLayoutResult.fontSizePx() = with(rule.density) {
        layoutInput.style.fontSize.toPx()
    }

    /** Every label of [layouts] is whole on one line within [roomPx], and they all share one size. */
    private fun assertWholeOnOneLine(what: String, layouts: List<TextLayoutResult>, roomPx: Float) {
        val broken = layouts.filter {
            it.lineCount != 1 || it.hasVisualOverflow ||
                it.lineWidth() > roomPx
        }
            .map {
                "${it.layoutInput.text.text} (${it.lineCount} lines, ${it.lineWidth()} px of $roomPx)"
            }
        assertEquals("$what that wrap, are cut or overflow", emptyList<String>(), broken)
        assertEquals(
            "$what sizes",
            1,
            layouts.map {
                it.layoutInput.style.fontSize
            }.distinct().size
        )
    }

    private fun assertLandscape() {
        val numbers = dayNumbers()
        assertEquals((1..DAYS).toList(), numbers.map { it.layoutInput.text.text.toInt() })
        val room = cellPx() - with(rule.density) { NUMBER_INSETS.toPx() }
        assertWholeOnOneLine("Day numbers", numbers, room)
        assertWholeOnOneLine("Weekday initials", initials(), cellPx())
        // Still larger than at 100%: the size steps down only as far as the cell needs.
        assertTrue(
            numbers.first().fontSizePx() >
                with(rule.density) { labelSmall.fontSize.value.dp.toPx() }
        )
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-w640dp-h360dp-land-xxhdpi"
    )
    fun `English, landscape 640 x 360 at 200 percent - every day number is whole on one line`() {
        show(fontScale = 2f, calendarWidth = LANDSCAPE_CALENDAR)

        assertLandscape()
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "de-w640dp-h360dp-land-xxhdpi"
    )
    fun `German, landscape 640 x 360 at 200 percent - every day number is whole on one line`() {
        show(fontScale = 2f, calendarWidth = LANDSCAPE_CALENDAR, initials = GERMAN_INITIALS)

        assertLandscape()
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-w640dp-h360dp-land-xxhdpi"
    )
    fun `A cell too narrow even at 100 percent keeps the 100 percent size, on one line`() {
        show(fontScale = 2f, calendarWidth = TOO_NARROW_CALENDAR)

        val numbers = dayNumbers()
        val hundredPercent = with(rule.density) { labelSmall.fontSize.value.dp.toPx() }
        numbers.forEach {
            assertEquals(it.layoutInput.text.text, hundredPercent, it.fontSizePx(), 0.05f)
        }
        assertTrue(numbers.all { it.lineCount == 1 })
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-w360dp-h800dp-port-xxhdpi"
    )
    fun `Portrait 360 dp at 200 percent keeps the person's own size (negative control)`() {
        show(fontScale = 2f, calendarWidth = null)

        val personsSize = with(rule.density) { labelSmall.fontSize.toPx() }
        dayNumbers().forEach {
            assertEquals(it.layoutInput.text.text, personsSize, it.fontSizePx(), 0f)
        }
        assertWholeOnOneLine(
            "Day numbers",
            dayNumbers(),
            cellPx() - with(rule.density) { NUMBER_INSETS.toPx() }
        )
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-w640dp-h360dp-land-xxhdpi"
    )
    fun `TalkBack hears each day as before - its description and chosen state, never its number`() {
        show(fontScale = 2f, calendarWidth = LANDSCAPE_CALENDAR)

        val chosen = rule.onNodeWithContentDescription("Day $CHOSEN").fetchSemanticsNode().config
        assertEquals(listOf("Day $CHOSEN"), chosen[SemanticsProperties.ContentDescription])
        assertEquals("chosen", chosen[SemanticsProperties.StateDescription])
        assertEquals(Role.Button, chosen[SemanticsProperties.Role])
        val future = rule.onNodeWithContentDescription("Day 30").fetchSemanticsNode().config
        assertEquals(listOf("Day 30"), future[SemanticsProperties.ContentDescription])
        listOf(chosen, future).forEach { config ->
            assertFalse("a number is heard", SemanticsProperties.Text in config)
            assertFalse("a test hook merged", ShownTextLayoutsKey in config)
        }
    }
}
