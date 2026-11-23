package com.migrely.app.designsystem.picker

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.TalkBackTree
import com.migrely.app.test.talkback.turnOnTalkBack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The stacked top row for TalkBack (T23 8.1 row R02, cr-de round 2 F6 (c)): with the title on its own line under the
 * buttons, the title is still a heading, and TalkBack goes Cancel, Done, then the title (cr-de accepted this order).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h640dp-port-xxhdpi")
class PickerTopRowTalkBackTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun showStacked() {
        RuntimeEnvironment.setFontScale(2f)
        rule.runOnUiThread { rule.activity.turnOnTalkBack() }
        rule.setContent {
            MigrelyTheme {
                PickerTopRow(
                    title = TITLE,
                    cancel = { TextButton(onClick = {}) { Text("Abbrechen") } },
                    confirm = { TextButton(onClick = {}) { Text("Fertig") } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
        }
    }

    @Test
    fun `Given the stacked title then it is a heading`() {
        showStacked()

        rule.onNode(
            hasText(TITLE)
        ).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test
    fun `Given the stacked title then TalkBack goes Cancel, Done, then the title`() {
        showStacked()
        val cancel = rule.onNode(hasText("Abbrechen") and hasClickAction()).fetchSemanticsNode().id
        val done = rule.onNode(hasText("Fertig") and hasClickAction()).fetchSemanticsNode().id
        val title = rule.onNode(hasText(TITLE)).fetchSemanticsNode().id

        var order = emptyList<Int>()
        rule.runOnUiThread {
            order = TalkBackTree(rule.activity).order(listOf(cancel, done, title))
        }

        assertEquals(listOf(cancel, done, title), order)
    }

    private companion object {
        const val TITLE = "Nächster Termin"
    }
}
