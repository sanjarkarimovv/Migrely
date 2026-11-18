package com.migrely.app.medication.ui.view

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.DpRect
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.ui.model.IntakeUiModel
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel
import java.util.concurrent.CountDownLatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** More than the 200 ms leave fade (BRD EC-UX-08), in 16 ms frames. */
private const val FRAMES = 20
private const val SETTLE_MILLIS = 1_000L
private const val SAVED = "Saved."
private const val ADD_AND_LOG = "Add and log"
private const val IBUPROFEN_ONLY_LOGGED = "Ibuprofen logged at"
private const val IBUPROFEN_STOP = "Log Ibuprofen, 400 mg"

private val INTAKE = IntakeUiModel(
    id = "intake-x",
    medicationName = IBUPROFEN_NAME,
    dose = "400 mg",
    takenAtEpochMillis = 1_795_062_000_000L,
    zoneId = "Asia/Tashkent",
    attackId = null
)

/** What one frame showed: the logged line, the old message, and the nodes that take room on screen. */
private data class Frame(val logged: Boolean, val message: Boolean, val nodesWithRoom: Set<Int>)

/**
 * A stored intake clears the message on Log medication (BRD D190 (1), EC-MED-16): silently, in the frame the logged
 * result appears, with no fade; only a stored intake clears it, never the tap or a failure.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class LogMedicationMessagesTest {
    @get:Rule
    val rule = createComposeRule()

    private val screen = LogMedicationHarness(rule, FakeDiary(IBUPROFEN_MEDICATION))
    private val diary get() = screen.diary

    private fun post(message: MedicationMessageUiModel, text: String) {
        screen.post(MedicationEventUiModel.Message(message))
        screen.awaitText(text)
    }

    /** Waits for the outcome of the tap that made write number [writes]: the buttons are enabled again. */
    private fun awaitOutcomeOfWrite(writes: Int) {
        screen.waitFor { diary.addCalls.size == writes }
        screen.awaitNode(screen.ibuprofen and isEnabled())
    }

    /**
     * Shows [text], hands the screen a logged intake with the clock stopped, and checks every frame: none shows the
     * logged result with the old message, and in none with the result does a node that the settled screen lacks take
     * any room, so no leaving message is drawn over it. The host's tap guard has no semantics and keeps its place,
     * as it should (design D2).
     */
    private fun assertClearsInTheLoggedFrame(
        message: MedicationMessageUiModel,
        text: String,
        darkRoom: Boolean = false
    ) {
        screen.show(darkRoom)
        post(message, text)
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false

        screen.post(MedicationEventUiModel.Logged(INTAKE))
        val frames = (1..FRAMES).map {
            rule.mainClock.advanceTimeByFrame()
            Frame(screen.exists(screen.loggedLine), screen.exists(hasText(text)), screen.nodesWithRoom())
        }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        val settled = screen.nodesWithRoom()
        rule.mainClock.autoAdvance = true

        val withResult = frames.filter { it.logged }
        assertTrue("the logged result never showed", withResult.isNotEmpty())
        withResult.forEach {
            assertFalse("the old message shows with the logged result", it.message)
            assertEquals(
                "a leaving node takes room over the logged result",
                emptySet<Int>(),
                it.nodesWithRoom - settled
            )
        }
        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    // 3.1 A stored intake clears every message, at once (D1, D2)

    @Test
    fun `A dose logged after freeing space clears the storage error`() {
        screen.show()
        screen.failOnFullStorage()

        screen.logIbuprofen()
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
        rule.onNode(screen.loggedLine).assertExists()
        rule.onNodeWithText(SAVED).assertDoesNotExist()
        assertEquals(1, diary.intakes.size)
    }

    @Test
    fun `The storage error goes in the frame the logged result appears`() =
        assertClearsInTheLoggedFrame(MedicationMessageUiModel.StorageFull, STORAGE_FULL_TEXT)

    @Test
    fun `Something went wrong goes in the frame the logged result appears`() =
        assertClearsInTheLoggedFrame(MedicationMessageUiModel.SomethingWentWrong, GENERIC_TEXT)

    @Test
    fun `Pick a time up to now goes in the frame the logged result appears`() =
        assertClearsInTheLoggedFrame(MedicationMessageUiModel.InFuture, IN_FUTURE_TEXT)

    @Test
    fun `Removed, nothing was logged, goes in the frame the logged result appears`() =
        assertClearsInTheLoggedFrame(MedicationMessageUiModel.IntakeUndone, INTAKE_UNDONE_TEXT)

    // 3.2 Only a stored intake clears (EC-MED-16 "It fails again", "Not on the tap")

    @Test
    fun `A tap that fails again keeps the list and the error`() {
        screen.show()
        screen.failOnFullStorage()
        diary.addFailure = AddIntakeResultDomainModel.StorageFull

        screen.tapIbuprofen()
        awaitOutcomeOfWrite(writes = 2)

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertExists()
        rule.onNode(screen.loggedLine).assertDoesNotExist()
        assertTrue(diary.intakes.isEmpty())
    }

    @Test
    fun `A tap that fails another way keeps the list and shows its error`() {
        screen.show()
        screen.failOnFullStorage()
        diary.addFailure = AddIntakeResultDomainModel.Failed

        screen.tapIbuprofen()
        awaitOutcomeOfWrite(writes = 2)

        screen.awaitText(GENERIC_TEXT)
        rule.onNode(screen.loggedLine).assertDoesNotExist()
        rule.onNode(screen.ibuprofen).assertExists()
    }

    @Test
    fun `A time in the future after an error keeps the list and shows its message`() {
        screen.show()
        screen.failOnFullStorage()

        post(MedicationMessageUiModel.InFuture, IN_FUTURE_TEXT)
        rule.waitForIdle()

        rule.onNode(screen.loggedLine).assertDoesNotExist()
        rule.onNode(screen.ibuprofen).assertExists()
    }

    @Test
    fun `A refused medication after an error keeps the error and logs nothing`() {
        screen.show()
        screen.failOnFullStorage()

        // A refused first medication keeps today's behaviour here (D190); #239 (F9) owns its own clear.
        screen.post(MedicationEventUiModel.NotSaved(SaveProblemUiModel.InvalidName))
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertExists()
        rule.onNode(screen.loggedLine).assertDoesNotExist()
    }

    @Test
    fun `While the write runs the error stays, and goes when the intake is stored`() {
        screen.show()
        screen.failOnFullStorage()
        val release = CountDownLatch(1)
        diary.holdAdd = release

        screen.tapIbuprofen()
        screen.waitFor { diary.addCalls.size == 2 }
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertExists()
        rule.onNode(screen.loggedLine).assertDoesNotExist()
        release.countDown()
        screen.awaitNode(screen.loggedLine)
        rule.waitForIdle()
        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
    }

    @Test
    fun `An error after a logged intake shows`() {
        screen.show()
        screen.post(MedicationEventUiModel.Logged(INTAKE))
        screen.awaitNode(screen.loggedLine)

        post(MedicationMessageUiModel.StorageFull, STORAGE_FULL_TEXT)

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertExists()
    }

    // 3.3 "Add and log" (EC-MED-16 "First medication added and logged")

    @Test
    fun `The first medication added and logged after freeing space clears the error`() {
        val first = LogMedicationHarness(rule, FakeDiary())
        first.show(firstContent = hasText(ADD_AND_LOG))
        rule.onNode(hasSetTextAction() and hasText("Name")).performTextInput(IBUPROFEN_NAME)
        first.diary.createFailure = MedicationWriteResultDomainModel.StorageFull
        rule.onNodeWithText(ADD_AND_LOG).performClick()
        first.awaitText(STORAGE_FULL_TEXT)
        first.diary.createFailure = null

        rule.onNodeWithText(ADD_AND_LOG).performClick()
        first.awaitNode(hasText(IBUPROFEN_ONLY_LOGGED, substring = true))
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
        assertEquals(listOf(IBUPROFEN_NAME), first.diary.medications.map { it.name })
        assertEquals(1, first.diary.intakes.size)
    }

    // 3.5 Rotation and process death (EC-MED-16, EC-MED-17)

    @Test
    fun `A restored screen after a log that cleared an error shows the result and no message`() {
        val restoration = StateRestorationTester(rule)
        screen.show(restoration = restoration)
        screen.failOnFullStorage()
        screen.logIbuprofen()

        restoration.emulateSavedInstanceStateRestore()
        screen.awaitNode(screen.loggedLine)
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
    }

    @Test
    fun `A restored screen during a running Undo shows the result with Undo enabled`() {
        val restoration = StateRestorationTester(rule)
        screen.show(restoration = restoration)
        screen.logIbuprofen()
        val release = CountDownLatch(1)
        diary.holdDelete = release
        rule.onNodeWithText("Undo").performClick()
        screen.waitFor { diary.deleteCalls.isNotEmpty() }

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        rule.onNode(screen.loggedLine).assertExists()
        rule.onNode(hasText("Undo") and isEnabled()).assertExists()
        release.countDown()
    }

    // 3.6 Dark room, keyboard, largest font and landscape

    @Test
    fun `In the dark room the error goes in the frame the logged result appears`() =
        assertClearsInTheLoggedFrame(
            MedicationMessageUiModel.StorageFull,
            STORAGE_FULL_TEXT,
            darkRoom = true
        )

    @Test
    fun `A log by Enter clears the error, leaves nothing focused, and only the logged line is live`() {
        screen.show()
        screen.failOnFullStorage()
        rule.onNodeWithContentDescription("Back").requestFocus()
        repeat(FOCUS_STOPS) {
            if (rule.focusedName() != IBUPROFEN_STOP) rule.press(Key.Tab)
        }
        assertEquals(IBUPROFEN_STOP, rule.focusedName())

        rule.press(Key.Enter)
        screen.awaitNode(screen.loggedLine)
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
        assertNull(rule.focusedName())
        val live = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
            .fetchSemanticsNodes()
        assertEquals(1, live.size)
        assertTrue(
            live.single().config[SemanticsProperties.Text].first().text.startsWith(LOGGED_IBUPROFEN)
        )
        assertEquals(listOf("Back", "Done"), rule.walk(2))
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `At the largest font in portrait the logged result is whole and uncovered`() =
        assertWholeAtLargestFont()

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `At the largest font in landscape the logged result is whole and uncovered`() =
        assertWholeAtLargestFont()

    private fun assertWholeAtLargestFont() {
        screen.show(fontScale = LARGEST_FONT)
        screen.failOnFullStorage()

        screen.logIbuprofen()
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
        val window = rule.onRoot().getUnclippedBoundsInRoot()
        listOf(rule.onNode(screen.loggedLine), rule.onNodeWithText("Done"), rule.onNodeWithText("Undo"))
            .forEach { node ->
                val bounds = node.getUnclippedBoundsInRoot()
                assertTrue("$bounds is not inside $window", window.contains(bounds))
            }
    }

    private companion object {
        const val FOCUS_STOPS = 6
        const val LARGEST_FONT = 2f
    }
}

private fun DpRect.contains(other: DpRect) =
    other.left >= left && other.top >= top && other.right <= right && other.bottom <= bottom
