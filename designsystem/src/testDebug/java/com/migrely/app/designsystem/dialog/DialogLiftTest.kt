package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val OLDER = "older"
private const val UPPER = "upper"
private const val PAST_THE_WAIT = 800L
private const val IN_THE_WAIT = 300L
private const val UNDER_THE_TIMEOUT = 100L
private const val PAST_THE_TIMEOUT = 400L
private const val FRAMES = 5
private const val BEYOND_DP_AT_MDPI = 350f
private const val XXHDPI = 3f
private const val INSIDE_THE_SLOP_DP = 90f
private const val OUTSIDE_THE_SLOP_DP = 110f

/**
 * A dialog over a dialog, in one test's windows: a lift on the upper one counts for the older one,
 * at screen positions read from the nodes (BRD D161). Robolectric's windows share an origin, so
 * that the two windows' origins agree is proved on a device, by the androidTest in `app`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class DialogLiftTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val harness = DialogWaitHarness(composeRule)
    private var olderClicks by mutableIntStateOf(0)
    private var upperClicks by mutableIntStateOf(0)
    private var upperOpen by mutableStateOf(true)
    private var upperCloses = false

    @Before
    fun setUp() = harness.setUp()

    private fun showBoth() = harness.show {
        MigrelyDialog(onDismissRequest = {}, fullScreen = true) {
            Surface {
                Box(
                    Modifier
                        .fillMaxSize()
                        .testTag(OLDER)
                        .clickable { olderClicks++ }
                )
            }
        }
        if (upperOpen) {
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    TextButton(
                        onClick = {
                            upperClicks++
                            if (upperCloses) upperOpen = false
                        },
                        modifier = Modifier.testTag(UPPER)
                    ) {
                        Text("Do")
                    }
                }
            )
        }
    }

    private fun upper() = harness.tagged(UPPER)

    private fun older() = harness.tagged(OLDER)

    private fun SemanticsNodeInteraction.centreOnScreen(): Offset {
        val node = fetchSemanticsNode()
        return node.positionOnScreen + Offset(node.size.width / 2f, node.size.height / 2f)
    }

    /** A press and lift on the older dialog at this point of the screen. */
    private fun pressOlderAt(onScreen: Offset) {
        val inside = onScreen - older().fetchSemanticsNode().positionOnScreen
        older().performTouchInput {
            down(inside)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun after(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun frames() = repeat(FRAMES) {
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
    }

    private fun assertOlderClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, olderClicks)
    }

    private fun tapUpperPastTheWait(): Offset {
        showBoth()
        harness.goTo(PAST_THE_WAIT)
        val point = upper().centreOnScreen()
        harness.tap(upper())
        composeRule.runOnIdle { assertEquals(1, upperClicks) }
        return point
    }

    @Test
    fun aPressOnTheOlderDialogUnderTheTimeoutDoesNothingAndAfterItAnswers() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_TIMEOUT)
        pressOlderAt(point)
        assertOlderClicks(0)

        after(PAST_THE_TIMEOUT)
        pressOlderAt(point)
        assertOlderClicks(1)
    }

    @Test
    fun aPressBeyondTheDistanceAnswers() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_TIMEOUT)
        pressOlderAt(point + Offset(0f, BEYOND_DP_AT_MDPI))

        assertOlderClicks(1)
    }

    @Test
    fun aLiftOnCancelCounts() {
        upperCloses = true
        val point = tapUpperPastTheWait()
        frames()
        upper().assertDoesNotExist()

        pressOlderAt(point)

        assertOlderClicks(0)
    }

    @Test
    fun aPressTheWaitIgnoredDoesNotCount() {
        upperOpen = false
        showBoth()
        harness.goTo(PAST_THE_WAIT)
        composeRule.runOnUiThread { upperOpen = true }
        frames()
        val point = upper().centreOnScreen()
        after(IN_THE_WAIT)
        harness.tap(upper())
        composeRule.runOnIdle { assertEquals(0, upperClicks) }

        composeRule.runOnUiThread { upperOpen = false }
        frames()
        pressOlderAt(point)

        assertOlderClicks(1)
    }

    @Test
    fun twoTapsInsideOneDialogBothCount() {
        tapUpperPastTheWait()

        after(UNDER_THE_TIMEOUT)
        harness.tap(upper())

        composeRule.runOnIdle { assertEquals(2, upperClicks) }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-xxhdpi")
    fun atXxhdpiNinetyDpFromTheLiftIsIgnored() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_TIMEOUT)
        pressOlderAt(point + Offset(0f, INSIDE_THE_SLOP_DP * XXHDPI))

        assertOlderClicks(0)
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-xxhdpi")
    fun atXxhdpiAHundredAndTenDpFromTheLiftAnswers() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_TIMEOUT)
        pressOlderAt(point + Offset(0f, OUTSIDE_THE_SLOP_DP * XXHDPI))

        assertOlderClicks(1)
    }
}
