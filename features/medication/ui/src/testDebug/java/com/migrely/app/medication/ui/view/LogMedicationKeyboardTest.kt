package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NAME = "Name"
private const val ADD_AND_LOG = "Add and log"
private const val ZONE = "Asia/Tashkent"
private const val TIME = 1_792_829_200_000L

// First use: no medication yet, so the form with its Name field shows.
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
 * Log medication's first-use form with the keyboard open near the page's end (BRD D189, EC-UX-61, EC-UX-65, #238):
 * closing the keyboard leaves the Name field where it is, in the light page and in the dark room of attack mode, so
 * no scroll is reported and TalkBack stays on the field.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LogMedicationKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val name get() = rule.onNode(hasSetTextAction() and hasText(NAME))

    private fun show(darkRoom: Boolean) {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MigrelyTheme {
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
        }
        rule.waitForIdle()
    }

    private fun scroll(): Float = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun closeNearTheEndKeepsTheName(darkRoom: Boolean) {
        show(darkRoom)
        rule.onNode(hasScrollAction()).performScrollToNode(hasSetTextAction() and hasText(NAME))
        name.performClick()
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(ADD_AND_LOG))
        rule.waitForIdle()
        assertTrue("the page fits above the keyboard", scroll() > 0f)
        val before = name.getUnclippedBoundsInRoot().top

        rule.runOnUiThread { rule.activity.setKeyboard(0) }
        rule.waitForIdle()

        assertEquals("the name moved", before.value, name.getUnclippedBoundsInRoot().top.value, 0.5f)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Closing the keyboard near the page's end leaves the name where it is`() =
        closeNearTheEndKeepsTheName(darkRoom = false)

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `In the dark room closing the keyboard near the page's end leaves the name where it is`() =
        closeNearTheEndKeepsTheName(darkRoom = true)
}
