package com.migrely.app.attack.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import com.migrely.app.attack.ui.model.AttackRangeUiModel
import com.migrely.app.attack.ui.model.PastAttackDraftUiModel
import com.migrely.app.attack.ui.model.PastAttackProblemUiModel
import com.migrely.app.designsystem.dialog.DIALOG_WAIT_MILLIS
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BACK = "Back"
private const val DISMISS = "Dismiss"
private const val ZONE = "Asia/Tashkent"

// 23 October 2026, 17:00 in Tashkent.
private const val NOW = 1_792_756_800_000L
private val DRAFT = PastAttackDraftUiModel(NOW - 7_200_000L, NOW, ZONE)
private val OVERLAPS = PastAttackProblemUiModel.Overlaps(
    listOf(AttackRangeUiModel(NOW - 10_800_000L, NOW - 3_600_000L))
)

/**
 * Add past migraine with a hardware keyboard (BRD D181 (6), EC-UX-51): the draft and overlap
 * orders with their reverse, focus on Started after "Change the times", and a picker that gives
 * focus back to its row.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1400dp")
class PastAttackKeyboardOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private var problem by mutableStateOf<PastAttackProblemUiModel?>(null)
    private var merges = 0
    private var adds = 0

    private val started = "Change when it started, now " + dateTime(DRAFT.startEpochMillis)
    private val ended = "Change when it ended, now " + dateTime(DRAFT.endEpochMillis)
    private val draftOrder get() = listOf(started, ended, "Add migraine")
    private val overlapOrder get() = listOf(started, ended, "Merge them", "Change the times")

    private fun dateTime(epochMillis: Long) = previewTimeFormatter.formatDateTime(epochMillis, ZONE)

    private fun show(withMessage: Boolean = false) = rule.setContent {
        MigrelyTheme {
            AddPastAttackContents(
                draft = DRAFT,
                problem = problem,
                saving = false,
                timeUiFormatter = previewTimeFormatter,
                actions = AddPastAttackActions(
                    onBack = {},
                    onStartPicked = {},
                    onEndPicked = {},
                    onAdd = { adds++ },
                    onMerge = { merges++ },
                    // As the screen does: the card leaves and nothing is stored.
                    onChangeTimes = { problem = null }
                ),
                messages = {
                    if (withMessage) {
                        MigrelySnackbarHost(message = "Couldn't save.", reduceMotion = true) {
                            MigrelySnackbar(it, SnackbarTone.Error, null, null, DISMISS, {})
                        }
                    }
                }
            )
        }
    }

    private fun focusOnBack() = rule.onNodeWithContentDescription(BACK).requestFocus()

    private fun assertOrderAndReverse(order: List<String>) {
        focusOnBack()
        assertEquals(order, rule.walk(order.size))
        assertEquals(order.reversed().drop(1) + BACK, rule.walk(order.size, back = true))
    }

    @Test
    fun `The draft runs Started, Ended, Add migraine, and reverses`() {
        show()
        assertOrderAndReverse(draftOrder)
    }

    @Test
    fun `With an overlap it runs Started, Ended, Merge them, Change the times, and reverses`() {
        problem = OVERLAPS
        show()
        assertOrderAndReverse(overlapOrder)
    }

    /** Negative control (task 5.3): an expected list with one stop dropped does not match. */
    @Test
    fun `The order check fails when a stop is missing`() {
        show()
        focusOnBack()

        assertNotEquals(draftOrder - ended, rule.walk(draftOrder.size))
    }

    @Test
    fun `With a message, its control comes after Back, then Started`() {
        show(withMessage = true)
        focusOnBack()

        assertEquals(listOf(DISMISS, started), rule.walk(2))
    }

    @Test
    fun `Enter on Change the times puts focus on Started, with nothing saved or merged`() {
        problem = OVERLAPS
        show()
        focusOnBack()
        rule.walk(overlapOrder.size)

        rule.press(Key.Enter)

        assertEquals(null, problem)
        assertEquals(started, rule.focusedName())
        assertEquals(0, adds + merges)
    }

    @Test
    fun `The Ended picker gives focus back to Ended`() {
        show()
        focusOnBack()
        rule.walk(2)

        rule.press(Key.Enter)
        // Past the dialog's guard against a tap meant for the screen below.
        rule.mainClock.advanceTimeBy(DIALOG_WAIT_MILLIS + 100)
        rule.onNodeWithText("Cancel").performClick()
        rule.waitForIdle()

        assertEquals(ended, rule.focusedName())
    }
}
