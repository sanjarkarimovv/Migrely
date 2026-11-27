package com.migrely.app.history.ui.view

import android.app.Application
import android.text.format.DateFormat
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import com.migrely.app.designsystem.layout.ShownTextLayoutsKey
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.mapper.CalendarLayoutUiMapper
import com.migrely.app.history.ui.mapper.DaySkyAppearanceUiMapper
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.mapper.HistoryTimeUiFormatter
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
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

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val MONDAY = LocalDate.of(2026, 10, 19)
private val TODAY = LocalDate.of(2026, 10, 23)

private fun millis(date: LocalDate, hour: Int, minute: Int) =
    date.atTime(LocalTime.of(hour, minute)).atZone(ZONE).toInstant().toEpochMilli()

// As in R25: "Mon, Oct 19, 12:27 PM – 12:27 PM" with no peak, one with a peak, and an attack still in progress.
private val ATTACKS = listOf(
    HistoryAttackUiModel("none", millis(MONDAY, 12, 27), ZONE.id, millis(MONDAY, 12, 27), null),
    HistoryAttackUiModel("seven", millis(MONDAY, 14, 5), ZONE.id, millis(MONDAY, 15, 40), 7),
    HistoryAttackUiModel("open", millis(TODAY, 12, 27), ZONE.id, null, null)
)

private fun mappers(locale: Locale, is24Hour: Boolean) = HistoryCalendarMappers(
    calendarLayoutUiMapper = CalendarLayoutUiMapper { locale },
    daySkyAppearanceUiMapper = DaySkyAppearanceUiMapper(),
    historyTimeUiFormatter = HistoryTimeUiFormatter(
        locale = { locale },
        is24HourFormat = { is24Hour },
        deviceZone = { ZONE },
        bestDateTimePattern = { patternLocale, skeleton ->
            DateFormat.getBestDateTimePattern(patternLocale, skeleton)
        }
    )
)

/** One History row: the date range and the peak, each with its node and its laid-out lines. */
private class ShownRow(val date: SemanticsNode, val peak: SemanticsNode) {
    val dateLayout: TextLayoutResult = date.config[ShownTextLayoutsKey].all.single()
    val peakLayout: TextLayoutResult = peak.config[ShownTextLayoutsKey].all.single()
    val dateText: String = dateLayout.layoutInput.text.text
    val peakText: String = peakLayout.layoutInput.text.text

    /** The peak has a line of its own, under the date. Unclipped: in portrait the list starts below the window. */
    val stacked: Boolean
        get() = peak.positionInRoot.y >= date.positionInRoot.y + date.size.height
}

/**
 * A History row keeps the peak beside the date only while the room beside it holds the date's widest unbreakable
 * piece; otherwise the peak goes under the date, which then breaks only between words (#275, BRD D181 (5), EC-HIS-07).
 * Before the fix, at 200% in landscape the date broke inside "12:27", "Mon," and "Okt.,".
 *
 * Landscape uses R25's geometry: panes of 275 dp, that is 640 dp less the 34 dp the emulator's side bar and cutout take
 * (Robolectric draws no insets), at xxhdpi like the emulator.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryAttackRowStackTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(locale: Locale, fontScale: Float, chosenDay: LocalDate? = null) {
        RuntimeEnvironment.setFontScale(fontScale)
        val month = previewMonth().let { preview ->
            preview.copy(
                attacks = ATTACKS,
                days = preview.days.map { day ->
                    day.copy(
                        attacks = ATTACKS.filter {
                            Instant.ofEpochMilli(it.startEpochMillis).atZone(ZONE).toLocalDate()
                                .toEpochDay() == day.epochDay
                        }
                    )
                }
            )
        }
        rule.setContent {
            MigrelyTheme {
                HistoryContents(
                    viewState = HistoryViewStateUiModel.Ready(month, null),
                    selectedEpochDay = chosenDay?.toEpochDay(),
                    mappers = mappers(locale, is24Hour = locale.language == "de"),
                    actions = previewActions,
                    messages = {},
                    overuseIndicator = {}
                )
            }
        }
        rule.waitForIdle()
    }

    /** The rows' texts, in order: each date range followed by its peak (day numbers and initials left out). */
    private fun rows(): List<ShownRow> =
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(ShownTextLayoutsKey), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .filter { node ->
                val layouts = node.config[ShownTextLayoutsKey].all
                layouts.size == 1 && !layouts.single().layoutInput.text.text.all(Char::isDigit)
            }
            .chunked(2) { (date, peak) -> ShownRow(date, peak) }

    /** Every line of the date ends at a space or at the end, the peak is one line, and nothing overflows. */
    private fun assertNothingBroken(row: ShownRow) {
        val layout = row.dateLayout
        (0 until layout.lineCount - 1).forEach { line ->
            val end = layout.getLineEnd(line)
            assertTrue(
                "\"${row.dateText}\" breaks inside a word after \"${row.dateText.substring(
                    0,
                    end
                )}\"",
                row.dateText[end - 1] == ' ' || row.dateText[end] == ' '
            )
        }
        assertFalse("\"${row.dateText}\" is cut", layout.hasVisualOverflow)
        assertEquals("\"${row.peakText}\" lines", 1, row.peakLayout.lineCount)
        assertFalse("\"${row.peakText}\" is cut", row.peakLayout.hasVisualOverflow)
    }

    /** No row breaks a word; the rows with [stackedPeak] have the peak under the date. */
    private fun assertRows(expectedRows: Int, stackedPeak: String) {
        val rows = rows()
        assertEquals(expectedRows, rows.size)
        rows.forEach(::assertNothingBroken)
        val stacking = rows.filter { it.peakText == stackedPeak }
        assertTrue("no \"$stackedPeak\" row", stacking.isNotEmpty())
        stacking.forEach { row ->
            assertTrue("\"${row.peakText}\" is not under \"${row.dateText}\"", row.stacked)
        }
    }

    private fun assertEveryRowSideBySide() {
        val rows = rows()
        assertEquals(ATTACKS.size, rows.size)
        rows.forEach { row ->
            assertNothingBroken(row)
            assertFalse("\"${row.peakText}\" went under \"${row.dateText}\"", row.stacked)
        }
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-rUS-w606dp-h360dp-land-xxhdpi"
    )
    fun `English, landscape at 200 percent - Not recorded goes under, no word breaks`() {
        show(Locale.US, fontScale = 2f)

        assertRows(ATTACKS.size, stackedPeak = "Not recorded")
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "de-rDE-w606dp-h360dp-land-xxhdpi"
    )
    fun `German, landscape at 200 percent - Nicht erfasst goes under, the time stays whole`() {
        show(Locale.GERMANY, fontScale = 2f)

        assertRows(ATTACKS.size, stackedPeak = "Nicht erfasst")
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-rUS-w606dp-h360dp-land-xxhdpi"
    )
    fun `English, landscape at 200 percent - the chosen day's list stacks the same way`() {
        show(Locale.US, fontScale = 2f, chosenDay = MONDAY)

        assertRows(2, stackedPeak = "Not recorded")
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "de-rDE-w640dp-h360dp-land-xxhdpi"
    )
    fun `German, landscape 640 dp with no insets at 200 percent - no word breaks`() {
        show(Locale.GERMANY, fontScale = 2f)

        val rows = rows()
        assertEquals(ATTACKS.size, rows.size)
        rows.forEach(::assertNothingBroken)
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-rUS-w606dp-h360dp-land-xxhdpi"
    )
    fun `A stacked row says the same and stays one button`() {
        show(Locale.US, fontScale = 2f)

        val row = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Role))
            .fetchSemanticsNodes()
            .single { node ->
                node.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }
                    .any { it.startsWith("Monday, October 19, 12:27 PM to") }
            }
        assertEquals(
            listOf(
                "Monday, October 19, 12:27 PM to 12:27 PM, peak not recorded. Opens its details."
            ),
            row.config[SemanticsProperties.ContentDescription]
        )
        assertEquals(Role.Button, row.config[SemanticsProperties.Role])
        assertFalse("the row's words are heard again", SemanticsProperties.Text in row.config)
        assertFalse("a test hook merged", ShownTextLayoutsKey in row.config)
        assertTrue("the row is not stacked", rows().first().stacked)
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-rUS-w360dp-h640dp-port-xxhdpi"
    )
    fun `Portrait 360 dp at 100 percent - the peak stays beside the date (negative control)`() {
        show(Locale.US, fontScale = 1f)

        assertEveryRowSideBySide()
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-rUS-w360dp-h640dp-port-xxhdpi"
    )
    fun `Portrait 360 dp at 200 percent - the peak stays beside the date (negative control)`() {
        show(Locale.US, fontScale = 2f)

        assertEveryRowSideBySide()
    }

    @Test
    @Config(
        sdk = [35],
        application = Application::class,
        qualifiers = "en-rUS-w411dp-h891dp-port-xxhdpi"
    )
    fun `Portrait 411 dp at 200 percent - the peak stays beside the date (negative control)`() {
        show(Locale.US, fontScale = 2f)

        assertEveryRowSideBySide()
    }
}
