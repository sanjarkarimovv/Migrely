package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BUTTON = "button"
private const val ROW = "row"
private const val SLIDER = "slider"
private const val PAGE = "page"
private const val IN_THE_WAIT = 300L
private const val AT_THE_END = DIALOG_WAIT_MILLIS
private const val AFTER_THE_WAIT = 800L
private const val SEMANTIC_ACTION_TIME = 50L
private const val THE_DIALS_HOUR = 8
private const val JANUARY_2026_UTC = 1_767_225_600_000L

/**
 * Through each wrapper, in a real dialog window on Robolectric, at gaps read from the dialog's own
 * clock. A text field is tested on a device, in the androidTest of this class's name: in a dialog
 * on Robolectric, even a raw one, it runs the test JVM out of memory while it first measures.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class DialogWaitTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val harness = DialogWaitHarness(composeRule)
    private var clicks by mutableIntStateOf(0)

    @Before
    fun setUp() = harness.setUp()

    @Composable
    private fun CountingButton() {
        TextButton(onClick = { clicks++ }, modifier = Modifier.testTag(BUTTON)) { Text("Do") }
    }

    private fun showAlert() = harness.show {
        MigrelyAlertDialog(onDismissRequest = {}, confirmButton = { CountingButton() })
    }

    private fun showDatePicker() = harness.show {
        MigrelyDatePickerDialog(onDismissRequest = {}, confirmButton = { CountingButton() }) {
            Text("Pick")
        }
    }

    private fun showPlain() = harness.show {
        MigrelyDialog(onDismissRequest = {}) { Surface { CountingButton() } }
    }

    private fun showFullScreen() = harness.show {
        MigrelyDialog(onDismissRequest = {}, fullScreen = true) { Surface { CountingButton() } }
    }

    private fun tapButton() = harness.tap(harness.tagged(BUTTON))

    private fun assertClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, clicks)
    }

    private fun assertTheButtonWaits(show: () -> Unit) {
        show()
        harness.goTo(IN_THE_WAIT)
        tapButton()
        assertClicks(0)

        harness.goTo(AT_THE_END)
        tapButton()

        assertClicks(1)
    }

    @Test
    fun theAlertDialogsButtonWaits() = assertTheButtonWaits(::showAlert)

    @Test
    fun theDatePickerDialogsButtonWaits() = assertTheButtonWaits(::showDatePicker)

    @Test
    fun thePlainDialogsButtonWaits() = assertTheButtonWaits(::showPlain)

    @Test
    fun theFullScreenDialogsButtonWaits() = assertTheButtonWaits(::showFullScreen)

    @Test
    fun aPressHeldAcrossTheEndDoesNothingAndTheNextAnswers() {
        showAlert()
        harness.goTo(IN_THE_WAIT)
        harness.tagged(BUTTON).performTouchInput { down(center) }
        composeRule.waitForIdle()
        harness.goTo(AFTER_THE_WAIT)
        harness.tagged(BUTTON).performTouchInput { up() }
        composeRule.waitForIdle()
        assertClicks(0)

        tapButton()

        assertClicks(1)
    }

    @Test
    fun aClickActionInsideTheWaitRuns() {
        showAlert()
        harness.goTo(SEMANTIC_ACTION_TIME)

        harness.tagged(BUTTON).performSemanticsAction(SemanticsActions.OnClick)

        assertClicks(1)
    }

    @Test
    fun aKeyboardInsideTheWaitActivates() {
        showAlert()
        harness.goTo(SEMANTIC_ACTION_TIME)
        harness.tagged(BUTTON).performSemanticsAction(SemanticsActions.RequestFocus)
        composeRule.waitForIdle()

        harness.tagged(BUTTON).performKeyInput { pressKey(Key.Enter) }

        assertClicks(1)
    }

    @Test
    fun aRadioRowWaits() {
        var chosen by mutableStateOf(false)
        harness.show {
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag(ROW)
                            .selectable(selected = chosen, onClick = { chosen = true })
                    ) {
                        RadioButton(selected = chosen, onClick = null)
                    }
                }
            )
        }
        harness.goTo(IN_THE_WAIT)
        harness.tap(harness.tagged(ROW))
        composeRule.runOnIdle { assertEquals(false, chosen) }

        harness.goTo(AT_THE_END)
        harness.tap(harness.tagged(ROW))

        composeRule.runOnIdle { assertEquals(true, chosen) }
    }

    @Test
    fun aSliderDoesNotMoveInTheWaitAndMovesAfter() {
        var value by mutableFloatStateOf(0f)
        harness.show {
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = {
                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier.fillMaxWidth().testTag(SLIDER)
                    )
                }
            )
        }
        harness.goTo(IN_THE_WAIT)
        harness.tap(harness.tagged(SLIDER))
        assertEquals("a press in the wait moved it", 0f, composeRule.runOnIdle { value })

        harness.goTo(AT_THE_END)
        harness.tap(harness.tagged(SLIDER))

        assertTrue("a tap after the wait moves it", composeRule.runOnIdle { value } > 0f)
    }

    @Test
    fun aClockDialDoesNotMoveInTheWaitAndMovesAfter() {
        var hour = { -1 }
        harness.show {
            MigrelyDialog(onDismissRequest = {}) {
                val state = rememberTimePickerState(
                    initialHour = THE_DIALS_HOUR,
                    initialMinute = 0,
                    is24Hour = true
                )
                hour = { state.hour }
                Surface { TimePicker(state = state) }
            }
        }
        val threeOClock = harness.matching(hasContentDescription("3 hours"))
        harness.goTo(IN_THE_WAIT)
        harness.tap(threeOClock)
        assertEquals(
            "a press in the wait moved it",
            THE_DIALS_HOUR,
            composeRule.runOnIdle {
                hour()
            }
        )

        harness.goTo(AFTER_THE_WAIT)
        harness.tap(threeOClock)
        composeRule.mainClock.advanceTimeBy(AFTER_THE_WAIT)

        assertNotEquals(
            "a tap after the wait moves it",
            THE_DIALS_HOUR,
            composeRule.runOnIdle {
                hour()
            }
        )
    }

    @Test
    fun aCalendarDayIsNotPickedInTheWaitAndIsAfter() {
        var picked: () -> Long? = { null }
        harness.show {
            MigrelyDatePickerDialog(onDismissRequest = {}, confirmButton = {}) {
                val state = rememberDatePickerState(initialDisplayedMonthMillis = JANUARY_2026_UTC)
                picked = { state.selectedDateMillis }
                DatePicker(state = state, title = null, headline = null, showModeToggle = false)
            }
        }
        val theFifteenth = harness.matching(
            hasText("January 15, 2026", substring = true) and hasClickAction()
        )
        harness.goTo(IN_THE_WAIT)
        harness.tap(theFifteenth)
        assertNull("a press in the wait picked it", composeRule.runOnIdle { picked() })

        harness.goTo(AT_THE_END)
        harness.tap(theFifteenth)

        assertNotEquals("a tap after the wait picks it", null, composeRule.runOnIdle { picked() })
    }

    @Test
    fun aDragInsideTheWaitDoesNothingAndTheNextScrolls() {
        val scrollState = ScrollState(0)
        harness.show {
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = {
                    Column(
                        Modifier
                            .heightIn(max = 200.dp)
                            .testTag(PAGE)
                            .verticalScroll(scrollState)
                    ) {
                        Box(Modifier.fillMaxWidth().height(1_000.dp))
                    }
                }
            )
        }
        harness.goTo(IN_THE_WAIT)
        dragThePage()
        assertEquals(0, composeRule.runOnIdle { scrollState.value })

        harness.goTo(AT_THE_END)
        dragThePage()

        assertTrue("the next drag scrolls", composeRule.runOnIdle { scrollState.value } > 0)
    }

    private fun dragThePage() {
        harness.tagged(PAGE).performTouchInput {
            down(center)
            repeat(3) { moveBy(Offset(0f, -60f)) }
            up()
        }
        composeRule.waitForIdle()
    }

    private fun assertSameTree(raw: @Composable () -> Unit, wrapped: @Composable () -> Unit) {
        harness.show(raw)
        val rawTree = harness.dialogTree()
        harness.hide()
        harness.show(wrapped)

        assertEquals(rawTree, harness.dialogTree())
        assertTrue("the tree has the button", rawTree.contains(BUTTON))
    }

    @Test
    fun theAlertDialogsTreeAndBoundsMatchARawOne() = assertSameTree(
        raw = { AlertDialog(onDismissRequest = {}, confirmButton = { CountingButton() }) },
        wrapped = {
            MigrelyAlertDialog(onDismissRequest = {}, confirmButton = { CountingButton() })
        }
    )

    @Test
    fun theDatePickerDialogsTreeAndBoundsMatchARawOne() = assertSameTree(
        raw = {
            DatePickerDialog(onDismissRequest = {}, confirmButton = { CountingButton() }) {
                Text("Pick")
            }
        },
        wrapped = {
            MigrelyDatePickerDialog(onDismissRequest = {}, confirmButton = { CountingButton() }) {
                Text("Pick")
            }
        }
    )

    @Test
    fun thePlainDialogsTreeAndBoundsMatchARawOne() = assertSameTree(
        raw = { Dialog(onDismissRequest = {}) { Surface { CountingButton() } } },
        wrapped = { MigrelyDialog(onDismissRequest = {}) { Surface { CountingButton() } } }
    )

    @Test
    fun nothingInTheTreeChangesWhenTheWaitEnds() {
        showAlert()
        val inTheWait = harness.dialogTree()

        harness.goTo(AFTER_THE_WAIT)

        assertEquals(inTheWait, harness.dialogTree())
    }
}
