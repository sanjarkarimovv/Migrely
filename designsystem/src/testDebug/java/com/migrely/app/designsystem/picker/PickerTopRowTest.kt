package com.migrely.app.designsystem.picker

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A full-screen picker's title never breaks inside a word (T23 8.1 row R02, D181): at 200% on a 360 dp phone German
 * "Nächster Termin" does not fit between "Abbrechen" and "Fertig", so it takes its own line.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h640dp-port-xxhdpi")
class PickerTopRowTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(title: String, cancel: String, confirm: String) {
        RuntimeEnvironment.setFontScale(2f)
        rule.setContent {
            MigrelyTheme {
                PickerTopRow(
                    title = title,
                    cancel = { TextButton(onClick = {}) { Text(cancel) } },
                    confirm = { TextButton(onClick = {}) { Text(confirm) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
        }
    }

    /** Every line of the title ends at a space or at the end of the text. */
    private fun assertNoWordBroken(title: String) {
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNode(hasText(title)).fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        val layout = layouts.single()
        (0 until layout.lineCount - 1).forEach { line ->
            val end = layout.getLineEnd(line)
            assertTrue(
                "\"$title\" breaks inside a word after \"${title.substring(0, end)}\"",
                title[end - 1] == ' ' || title[end] == ' '
            )
        }
    }

    @Test
    fun `Given German at 200 percent then the title breaks no word`() {
        show("Nächster Termin", "Abbrechen", "Fertig")

        assertNoWordBroken("Nächster Termin")
    }

    @Test
    fun `Given the range picker's German title at 200 percent then it breaks no word`() {
        show("Zeitraum wählen", "Abbrechen", "Fertig")

        assertNoWordBroken("Zeitraum wählen")
    }

    @Test
    fun `Given English at 200 percent then the title breaks no word`() {
        show("Next visit", "Cancel", "Done")

        assertNoWordBroken("Next visit")
    }
}
