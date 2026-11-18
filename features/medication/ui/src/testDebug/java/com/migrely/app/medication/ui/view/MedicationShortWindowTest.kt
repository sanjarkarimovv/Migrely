package com.migrely.app.medication.ui.view

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NAME = "Name"
private const val DOSE = "Usual dose (optional)"
private const val ZONE = "Asia/Tashkent"
private const val TIME = 1_792_829_200_000L

// Inside the field's outline, which is drawn in the cursor's colour while focused.
private val FIELD_INSIDE = 20.dp

// 8 dp at density 2.625 (ux U1), less a pixel of rounding.
private const val MARGIN_PX = 20f

private val FIRST_USE = MedicationLogUiModel(
    defaultTimeEpochMillis = TIME,
    zoneId = ZONE,
    attackId = "a",
    medications = emptyList()
)

private val FORMATTER = MedicationTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { ZoneId.of(ZONE) },
    bestDateTimePattern = { _, skeleton -> if (skeleton == "EEEdMMM") "EEE d MMM" else "HH:mm" }
)

/**
 * The input line being typed shows whole on Edit medication (name, dose) and Log medication (name, light and dark room)
 * in QA's short window (BRD D192, EC-UX-72; #245; task 3.2): API 36 with Gboard at 200% landscape, 360 dp tall, where on
 * v1.3.1 68 of the line's 112 px showed under the 64 dp bar. The bar steps aside while the keyboard is up; in the dark
 * room the area it leaves is the dark-room background; in the 411 dp window the bar stays.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], fontScale = 2f)
class MedicationShortWindowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var form by mutableStateOf<MedicationFormUiModel?>(
        MedicationFormUiModel(name = "Ibuprofen", kind = MedicationKindUiModel.Acute)
    )

    private var cursorColor = 0

    private fun field(label: String) = rule.onNode(hasSetTextAction() and hasText(label))

    private fun showEdit() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                cursorColor = OutlinedTextFieldDefaults.colors().cursorColor.toArgb()
                EditMedicationContents(
                    adding = false,
                    notFound = false,
                    form = form,
                    problem = null,
                    pending = false,
                    onFormChange = { form = it },
                    onSave = {},
                    onBack = {},
                    messages = {}
                )
            }
        }
        settle()
    }

    private fun showLog(darkRoom: Boolean) {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                cursorColor = if (darkRoom) {
                    MigrelyColors.AttackModeSelectedOutline.toArgb()
                } else {
                    OutlinedTextFieldDefaults.colors().cursorColor.toArgb()
                }
                LogMedicationContents(
                    log = FIRST_USE,
                    shownTime = TIME,
                    logged = null,
                    problem = null,
                    pending = false,
                    darkRoom = darkRoom,
                    timeUiFormatter = FORMATTER,
                    actions = LogMedicationActions(
                        onBack = {},
                        onTimePicked = {},
                        onMedication = {},
                        onAddAndLog = { _, _ -> },
                        onUndo = {},
                        onDone = {},
                        onManage = {}
                    ),
                    messages = {}
                )
            }
        }
        settle()
    }

    private fun settle() {
        rule.waitForIdle()
        setKeyboard(null)
    }

    private fun setKeyboard(top: Int?) {
        rule.runOnUiThread { rule.activity.setShortWindowInsets(top) }
        rule.waitForIdle()
    }

    /** As QA's harness: the page brought to the field, a tap, the keyboard, then typing; no drag after it opens. */
    private fun typeIn(label: String, keyboardTop: Int) {
        rule.onNode(hasScrollAction()).performScrollToNode(hasSetTextAction() and hasText(label))
        field(label).performClick()
        rule.waitForIdle()
        setKeyboard(keyboardTop)
        field(label).performTextInput("x")
        rule.waitForIdle()
    }

    /**
     * The caret's rows on screen, in px, as QA measures it: Compose draws the caret over the whole line box, so the
     * longest vertical run of the cursor's colour inside [field] (its outline, the same colour, left out) is the part of
     * the line being typed that shows. Null with no caret drawn.
     */
    private fun caretRows(field: SemanticsNodeInteraction): IntRange? {
        val density = rule.activity.resources.displayMetrics.density
        val bounds = field.getUnclippedBoundsInRoot()
        val inside = (FIELD_INSIDE.value * density).toInt()
        val window = drawWindow()
        val left = (bounds.left.value * density).toInt() + inside
        val right = (bounds.right.value * density).toInt() - inside
        var best: IntRange? = null
        (left until right).forEach { x ->
            var start = -1
            (0..window.height).forEach { y ->
                val caret = y < window.height && window.getPixel(x, y) == cursorColor
                if (caret && start < 0) start = y
                if (!caret && start >= 0) {
                    if (best == null || y - start > best!!.count()) best = start until y
                    start = -1
                }
            }
        }
        return best
    }

    /** The height of [field]'s line, as laid out. */
    private fun lineHeight(field: SemanticsNodeInteraction): Int {
        val results = mutableListOf<TextLayoutResult>()
        field.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return (results.first().getLineBottom(0) - results.first().getLineTop(0)).toInt()
    }

    private fun assertInputLineShows(label: String, margin: Float) {
        val line = lineHeight(field(label))
        val caret = checkNotNull(caretRows(field(label))) { "$label: no caret drawn" }
        val visible = rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "$label: ${caret.count()} of the line's $line px show, " +
                "at ${caret.first}..${caret.last + 1}, in the visible part ${visible.top}..${visible.bottom} (${visible.height} px)",
            caret.count() >= line - 1 &&
                caret.first - visible.top >= margin &&
                visible.bottom - (caret.last + 1) >= margin
        )
    }

    private fun assertInputLineShowsWithMargins(label: String) =
        assertInputLineShows(label, MARGIN_PX)

    private fun backInTree(): Boolean {
        val back = rule.activity.getString(R.string.medication_navigate_back)
        return rule.onAllNodes(hasContentDescription(back)).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `Edit medication's name in the 360 dp window shows whole with the bar aside`() {
        showEdit()

        typeIn(NAME, KEYBOARD_TOP_360_PX)

        assertInputLineShowsWithMargins(NAME)
        assertFalse("Back is in the tree with the bar aside", backInTree())
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `Edit medication's dose in the 360 dp window shows whole with the bar aside`() {
        showEdit()

        typeIn(DOSE, KEYBOARD_TOP_360_PX)

        assertInputLineShowsWithMargins(DOSE)
        assertFalse("Back is in the tree with the bar aside", backInTree())
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `Log medication's name in the 360 dp window shows whole with the bar aside`() {
        showLog(darkRoom = false)

        typeIn(NAME, KEYBOARD_TOP_360_PX)

        assertInputLineShowsWithMargins(NAME)
        assertFalse("Back is in the tree with the bar aside", backInTree())
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `In the dark room the area the bar leaves is the dark room's background`() {
        showLog(darkRoom = true)

        typeIn(NAME, KEYBOARD_TOP_360_PX)

        assertInputLineShowsWithMargins(NAME)
        // The status bar and where Back was, at the window's left edge, beside the page's padding.
        val window = drawWindow()
        val dark = MigrelyColors.AttackModeBackground.toArgb()
        val left = with(Density(rule.activity)) { 8.dp.roundToPx() }
        (0 until KEYBOARD_TOP_360_PX).filter { it < STATUS_PX + BAR_ROWS }.forEach { row ->
            (0 until left).forEach { x ->
                assertEquals("pixel ($x, $row)", dark, window.getPixel(x, row))
            }
        }
    }

    // Robolectric draws in software, where a layer's alpha is not applied: this sees colours, not fades.
    private fun drawWindow(): Bitmap {
        val decor = rule.activity.window.decorView
        val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { decor.draw(Canvas(bitmap)) }
        return bitmap
    }

    @Test
    @Config(qualifiers = WINDOW_411)
    fun `Edit medication's name in the 411 dp window keeps the bar`() {
        showEdit()
        val barBottom = rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot.top

        typeIn(NAME, KEYBOARD_TOP_411_PX)

        // Whole, the margins as the follow gives them today: Robolectric lays this field's line out lower than API 36
        // does (279 against QA's 250), so its margins here are not the device's.
        assertInputLineShows(NAME, margin = 0f)
        assertTrue("Back left the tree", backInTree())
        assertEquals(
            "the bar moved",
            barBottom,
            rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot.top,
            0.5f
        )
    }

    private companion object {
        // The 64 dp bar at density 2.625.
        const val BAR_ROWS = 168
    }
}
