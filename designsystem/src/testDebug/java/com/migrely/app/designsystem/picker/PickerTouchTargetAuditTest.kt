package com.migrely.app.designsystem.picker

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.TOUCH_BAR_DP
import com.migrely.app.test.talkback.touchTargets
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NEXT = "Next"
private const val PERCENT = 100
private const val DIALOG_WAIT_MILLIS = 2_000L
private val TODAY = LocalDate.of(2026, 11, 19)

/**
 * Touch-target audit of the Material date and time pickers (#255, design D5 T2 and T3; lead decision 2026-11-19,
 * option 1). Material draws their controls, so they cannot take a 48 dp node of their own: a picker with a control
 * under 48 dp keeps Compose's widening, stays out of exact bounds and keeps only the half step.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PickerTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun millis(date: LocalDate, hour: Int) =
        date.atTime(hour, 0).toInstant(ZoneOffset.UTC).toEpochMilli()

    /** Which of the date picker, the date-time picker's date step and its time step have a control under 48 dp. */
    private fun withControlsUnderBar(fontScale: Float): List<String> {
        var dateTime by mutableStateOf(false)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MigrelyTheme {
                    if (dateTime) {
                        MigrelyDateTimePicker(
                            initialEpochMillis = millis(TODAY, 9),
                            minEpochMillis = millis(TODAY.minusYears(1), 0),
                            maxEpochMillis = millis(TODAY, 23),
                            zoneId = ZoneId.of("UTC"),
                            labels = DateTimePickerLabels(
                                "When",
                                NEXT,
                                "Cancel",
                                "OK",
                                "Out of range"
                            ),
                            onConfirm = {},
                            onDismiss = {}
                        )
                    } else {
                        MigrelyDatePicker(
                            initial = TODAY,
                            firstDate = TODAY.minusYears(1),
                            lastDate = TODAY,
                            labels = DatePickerLabels("Visit", "OK", "Cancel", "No day"),
                            onConfirm = {},
                            onDismiss = {}
                        )
                    }
                }
            }
        }
        val found = mutableListOf<String>()
        fun collect(name: String) {
            rule.waitForIdle()
            val all = rule.touchTargets()
            val width = rule.activity.resources.configuration.screenWidthDp
            val smallest = all.minByOrNull { minOf(it.widthDp, it.heightDp) }
            val size = "${width}dp ${(fontScale * PERCENT).toInt()}%"
            println("AUDIT $name | $size | ${all.size} | $smallest | ${all.joinToString("; ")}")
            val under = all.filter { it.widthDp < TOUCH_BAR_DP || it.heightDp < TOUCH_BAR_DP }
            println("UNDER $name: ${under.size}: ${under.take(3)}")
            if (under.isNotEmpty()) found += name
        }
        collect("Date picker")
        rule.runOnIdle { dateTime = true }
        collect("Date-time picker, date")
        // Past the dialog's wait, which drops a tap right after it opens (BRD D160).
        rule.mainClock.advanceTimeBy(DIALOG_WAIT_MILLIS)
        rule.onNodeWithText(NEXT).performClick()
        collect("Date-time picker, time")
        return found
    }

    // What the measurement found (3.3): if Material's controls ever all reach 48 dp, this turns red and the pickers
    // can join exact bounds.
    private val expected = listOf("Date picker", "Date-time picker, date", "Date-time picker, time")

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `360 dp, 100 percent`() = assertEquals(expected, withControlsUnderBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `411 dp, 100 percent`() = assertEquals(expected, withControlsUnderBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `360 dp, 200 percent`() = assertEquals(expected, withControlsUnderBar(2f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `411 dp, 200 percent`() = assertEquals(expected, withControlsUnderBar(2f))
}
