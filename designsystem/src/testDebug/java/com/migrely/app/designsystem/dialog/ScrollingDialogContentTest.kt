package com.migrely.app.designsystem.dialog

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
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

private const val LARGEST_FONT = 2f
private const val TITLE = "Wie gut hat Ibuprofen-Lysinat Schmerztabletten 400 geholfen?"
private const val AREA = "area"
private val ANSWERS = listOf("Gar nicht", "Etwas", "Deutlich", "Vollständig")

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
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var outlineVariant = Color.Unspecified

    private fun show(answers: List<String> = ANSWERS, title: String = TITLE) {
        RuntimeEnvironment.setFontScale(LARGEST_FONT)
        rule.setContent {
            MigrelyTheme {
                outlineVariant = MaterialTheme.colorScheme.outlineVariant
                Box(Modifier.size(272.dp, 138.dp).clipToBounds()) {
                    ScrollingDialogContent(title, Modifier.testTag(AREA)) {
                        answers.forEach { answer ->
                            TextButton(onClick = {}, modifier = Modifier.heightIn(min = 48.dp)) {
                                Text(answer)
                            }
                        }
                    }
                }
            }
        }
    }

    /** Whether the area's top or bottom pixel row, in its middle, is the divider's colour, as the window draws it. */
    private fun dividerAt(top: Boolean): Boolean {
        rule.waitForIdle()
        val area = rule.onNodeWithTag(AREA).fetchSemanticsNode().boundsInWindow
        val decor = rule.activity.window.decorView
        val window = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { decor.draw(Canvas(window)) }
        val y = if (top) area.top.toInt() else area.bottom.toInt() - 1
        return window.getPixel(area.center.x.toInt(), y) == outlineVariant.toArgb()
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

    /** The title is the area's heading; the dialog around it names the pane, so the area names none. */
    @Test
    fun `Given the area then its title is a heading and it names no pane`() {
        show()

        assertEquals(
            1,
            rule.onAllNodes(hasText(TITLE) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
                .fetchSemanticsNodes().size
        )
        assertEquals(
            0,
            rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
                .fetchSemanticsNodes().size
        )
    }

    /**
     * T23 8.1 F3: in a real alert dialog, Material names its own pane "Dialog". With two names TalkBack spoke whichever
     * pane appeared last: "Dialog" for the pain and custom tag dialogs on the API 36 emulator, the title for the effect
     * dialog. The dialog's one pane is named by the title.
     */
    @Test
    fun `Given the area in an alert dialog then the dialog's one pane is named by the title`() {
        rule.setContent {
            MigrelyTheme {
                MigrelyAlertDialog(
                    onDismissRequest = {},
                    confirmButton = { TextButton(onClick = {}) { Text("OK") } },
                    paneTitle = TITLE,
                    text = { ScrollingDialogContent(TITLE) { Text(ANSWERS.first()) } }
                )
            }
        }

        val panes = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
            .fetchSemanticsNodes()
            .map { it.config[SemanticsProperties.PaneTitle] }
        assertEquals(listOf(TITLE), panes)
    }

    /** ux F6 follow-up: a fade in the surface colour over a blank-looking peek showed nothing (R04r). */
    @Test
    fun `Given more below then a divider shows at the bottom edge only`() {
        show()

        assertTrue("no divider at the bottom", dividerAt(top = false))
        assertFalse("a divider at the top", dividerAt(top = true))
    }

    @Test
    fun `Given the area scrolled to its end then a divider shows at the top edge only`() {
        show()
        rule.onNode(hasText("Vollständig")).performScrollTo()

        assertTrue("no divider at the top", dividerAt(top = true))
        assertFalse("a divider at the bottom", dividerAt(top = false))
    }

    @Test
    fun `Given content that fits then no divider shows`() {
        show(answers = listOf("Ja"), title = "Kurz")

        assertFalse("a divider at the top", dividerAt(top = true))
        assertFalse("a divider at the bottom", dividerAt(top = false))
    }
}
