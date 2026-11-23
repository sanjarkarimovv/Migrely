package com.migrely.app.designsystem.picker

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.TalkBackTree
import com.migrely.app.test.talkback.turnOnTalkBack
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The range picker's headline breaks no word (T23 8.1 row R02, ux A22): at 200% on a 360 dp phone Material's
 * "Startdatum - Enddatum" broke "Enddatum" letter by letter. Each side stays whole and the line wraps only between
 * them, with the dash kept on the start side; TalkBack hears one polite node naming both sides.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "de-w360dp-h640dp-port-xxhdpi")
class RangeHeadlineTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun show(
        labels: DateRangePickerLabels,
        start: LocalDate? = null,
        end: LocalDate? = null
    ) {
        RuntimeEnvironment.setFontScale(2f)
        rule.setContent {
            MigrelyTheme {
                DateRangePickerScreen(
                    state = rememberDateRangePickerState(
                        initialSelectedStartDateMillis = start?.utcMidnight(),
                        initialSelectedEndDateMillis = end?.utcMidnight(),
                        initialDisplayMode = DisplayMode.Picker
                    ),
                    labels = labels,
                    onConfirm = { _, _ -> },
                    onDismiss = {}
                )
            }
        }
    }

    /** The text exactly as shown, from the unmerged tree; every line of it ends at a space or at its end. */
    private fun assertWhole(shown: String) {
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNode(hasText(shown), useUnmergedTree = true).fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        val layout = layouts.single()
        (0 until layout.lineCount - 1).forEach { line ->
            val end = layout.getLineEnd(line)
            assertTrue(
                "\"$shown\" breaks inside a word after \"${shown.substring(0, end)}\"",
                shown[end - 1] == ' ' || shown[end] == ' '
            )
        }
    }

    @Test
    fun `Given German with no dates at 200 percent then both placeholders are whole`() {
        show(GERMAN)

        assertWhole("Startdatum$NBSP–")
        assertWhole("Enddatum")
    }

    @Test
    fun `Given German with both dates at 200 percent then both dates are whole`() {
        show(GERMAN, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 23))

        assertWhole("1.${NBSP}Okt.${NBSP}2026$NBSP–")
        assertWhole("23.${NBSP}Nov.${NBSP}2026")
    }

    @Test
    @Config(qualifiers = "en-w360dp-h640dp-port-xxhdpi")
    fun `Given English with no dates at 200 percent then both placeholders are whole`() {
        show(ENGLISH)

        assertWhole("Start${NBSP}date$NBSP–")
        assertWhole("End${NBSP}date")
    }

    @Test
    fun `Given no dates then TalkBack hears both sides as not chosen, politely`() {
        show(GERMAN)

        rule.onNode(hasContentDescription(NONE_CHOSEN)).assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
        )
    }

    @Test
    fun `Given a start only then TalkBack hears its date and the end as not chosen`() {
        show(GERMAN, LocalDate.of(2026, 10, 1))

        rule.onNode(
            hasContentDescription(
                "Startdatum, Donnerstag, 1. Oktober 2026, Enddatum, nicht gewählt"
            )
        ).assertExists()
    }

    /** D181 (2), #217: TalkBack says a merged node's Text after its description, so the shown words are hidden. */
    @Test
    fun `Given the headline then TalkBack hears only its one name`() {
        rule.runOnUiThread { rule.activity.turnOnTalkBack() }
        show(GERMAN)

        val headline = rule.onNode(hasContentDescription(NONE_CHOSEN), useUnmergedTree = true)
            .fetchSemanticsNode()
        var spoken = ""
        rule.runOnUiThread { spoken = TalkBackTree(rule.activity).spokenName(headline) }
        assertEquals(NONE_CHOSEN, spoken)
    }

    private companion object {
        const val NBSP = '\u00A0'
        const val NONE_CHOSEN = "Startdatum, nicht gewählt, Enddatum, nicht gewählt"
        val GERMAN = DateRangePickerLabels(
            title = "Zeitraum wählen",
            confirm = "Fertig",
            cancel = "Abbrechen",
            startDate = "Startdatum",
            endDate = "Enddatum",
            notChosen = "nicht gewählt",
            headlineDescription = "Startdatum, %1\$s, Enddatum, %2\$s"
        )
        val ENGLISH = DateRangePickerLabels(
            title = "Choose the dates",
            confirm = "Done",
            cancel = "Cancel",
            startDate = "Start date",
            endDate = "End date",
            notChosen = "not chosen",
            headlineDescription = "Start date, %1\$s, end date, %2\$s"
        )
    }
}
