package com.migrely.app.designsystem.dialog

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LARGEST_FONT = 2f
private const val TITLE = "Wie gut hat Ibuprofen-Lysinat Schmerztabletten 400 geholfen?"

/**
 * A dialog's title and content scroll together, so at the largest font in a short landscape window the last control
 * can still be brought into view whole (T23 8.1 rows R03, R04). The area is the 272 x 138 dp a dialog had there on the
 * API 36 emulator at 480 dpi; the system's font scale makes text grow as on the device.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h326dp-land-xxhdpi")
class ScrollingDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show() {
        RuntimeEnvironment.setFontScale(LARGEST_FONT)
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.size(272.dp, 138.dp).clipToBounds()) {
                    ScrollingDialogContent(TITLE) {
                        listOf("Gar nicht", "Etwas", "Deutlich", "Vollständig").forEach { answer ->
                            TextButton(onClick = {}, modifier = Modifier.heightIn(min = 48.dp)) {
                                Text(answer)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Given a long title then the last answer can be brought into view whole`() {
        show()

        rule.onNode(hasText("Vollständig")).performScrollTo()
        val shown = rule.onNode(hasText("Vollständig")).fetchSemanticsNode()
        assertTrue(
            "the last answer shows ${shown.boundsInRoot.height} of ${shown.size.height} px",
            shown.size.height > 0 && shown.boundsInRoot.height >= shown.size.height - 1f
        )
    }

    @Test
    fun `Given the area then it names the pane for TalkBack once`() {
        show()

        assertEquals(
            1,
            rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, TITLE))
                .fetchSemanticsNodes().size
        )
    }
}
