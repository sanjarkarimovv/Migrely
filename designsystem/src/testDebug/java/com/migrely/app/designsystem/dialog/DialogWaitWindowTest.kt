package com.migrely.app.designsystem.dialog

import android.os.SystemClock
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.migrely.app.designsystem.picker.DateRangePickerLabels
import com.migrely.app.designsystem.picker.MigrelyDateRangePicker
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

private const val IN_THE_WAIT = 300L
private const val AFTER_THE_WAIT = 800L

/** What only the dialog's window decides: a press outside the card, and Back. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class DialogWaitWindowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val harness = DialogWaitHarness(composeRule)
    private var dismissed by mutableIntStateOf(0)

    @Before
    fun setUp() = harness.setUp()

    private fun showAlert() = harness.show {
        MigrelyAlertDialog(onDismissRequest = {
            dismissed++
        }, confirmButton = {}, text = { Text("A") })
    }

    private fun showDatePicker() = harness.show {
        MigrelyDatePickerDialog(onDismissRequest = {
            dismissed++
        }, confirmButton = {}) { Text("A") }
    }

    private fun showPlain() = harness.show {
        MigrelyDialog(onDismissRequest = { dismissed++ }) { Surface { Text("A") } }
    }

    private fun pressOutside() = touchOutside(MotionEvent.ACTION_DOWN)

    private fun liftOutside() = touchOutside(MotionEvent.ACTION_UP)

    private fun tapOutside() {
        pressOutside()
        liftOutside()
    }

    /**
     * Beyond the window's corner: a modal window is given such a press, and its card can't contain
     * it.
     */
    private fun touchOutside(action: Int) {
        composeRule.runOnUiThread {
            val now = SystemClock.uptimeMillis()
            val event = MotionEvent.obtain(now, now, action, -1f, -1f, 0)
            ShadowDialog.getLatestDialog().dispatchTouchEvent(event)
            event.recycle()
        }
        composeRule.waitForIdle()
    }

    private fun assertDismissed(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, dismissed)
    }

    private fun assertAnOutsideTapWaits(show: () -> Unit) {
        show()
        harness.goTo(IN_THE_WAIT)
        tapOutside()
        assertDismissed(0)

        harness.goTo(AFTER_THE_WAIT)
        tapOutside()

        assertDismissed(1)
    }

    @Test
    fun anAlertDialogDoesNotCloseFromOutsideInTheWaitAndDoesAfter() =
        assertAnOutsideTapWaits(::showAlert)

    @Test
    fun aDatePickerDialogDoesNotCloseFromOutsideInTheWaitAndDoesAfter() =
        assertAnOutsideTapWaits(::showDatePicker)

    @Test
    fun aPlainDialogDoesNotCloseFromOutsideInTheWaitAndDoesAfter() =
        assertAnOutsideTapWaits(::showPlain)

    @Test
    fun aPressOutsideHeldAcrossTheEndDoesNotClose() {
        showAlert()
        harness.goTo(IN_THE_WAIT)
        pressOutside()
        harness.goTo(AFTER_THE_WAIT)
        liftOutside()

        assertDismissed(0)
    }

    @Test
    fun backInsideTheWaitClosesAtOnce() {
        showAlert()
        harness.goTo(IN_THE_WAIT)

        composeRule.runOnUiThread { ShadowDialog.getLatestDialog().onBackPressed() }

        assertDismissed(1)
    }

    @Test
    fun theRangePickerKeepsItsKeyboardModeAfterItsWait() {
        harness.show {
            MaterialTheme {
                MigrelyDateRangePicker(
                    initialStart = null,
                    initialEnd = null,
                    lastDate = LocalDate.of(2026, 1, 31),
                    yearRange = 2020..2026,
                    labels = DateRangePickerLabels(
                        title = "Range",
                        cancel = "Cancel",
                        confirm = "Done"
                    ),
                    onConfirm = { _, _ -> },
                    onDismiss = { dismissed++ }
                )
            }
        }

        harness.goTo(AFTER_THE_WAIT)

        composeRule.runOnIdle {
            val mode = ShadowDialog.getLatestDialog().window!!.attributes.softInputMode
            assertEquals(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
                mode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
            )
        }
    }
}
