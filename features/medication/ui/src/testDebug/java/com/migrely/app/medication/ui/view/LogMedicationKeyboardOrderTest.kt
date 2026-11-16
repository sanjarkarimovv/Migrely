package com.migrely.app.medication.ui.view

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import com.migrely.app.designsystem.dialog.DIALOG_WAIT_MILLIS
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.IntakeUiModel
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BACK = "Back"
private const val TAKEN_AT = "Taken at"
private const val IBUPROFEN = "Log Ibuprofen, 400 mg"
private const val SUMATRIPTAN = "Log Sumatriptan, 50 mg"
private const val MANAGE = "Manage medications"
private const val ZONE = "Asia/Tashkent"
private const val TIME = 1_792_829_200_000L

private val LIST_ORDER = listOf(TAKEN_AT, IBUPROFEN, SUMATRIPTAN, MANAGE)
private val FIRST_USE_ORDER = listOf(
    TAKEN_AT,
    "Name",
    "Simple painkiller",
    "Triptan",
    "Combination or other",
    "Not sure",
    "Add and log"
)

private val LOG = MedicationLogUiModel(
    defaultTimeEpochMillis = TIME,
    zoneId = ZONE,
    attackId = "a",
    medications = listOf(
        MedicationUiModel(
            "i",
            "Ibuprofen",
            MedicationKindUiModel.Acute,
            MedicationCategoryUiModel.SimplePainkiller,
            "400 mg"
        ),
        MedicationUiModel(
            "s",
            "Sumatriptan",
            MedicationKindUiModel.Acute,
            MedicationCategoryUiModel.Triptan,
            "50 mg"
        )
    )
)

private val FORMATTER = MedicationTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { ZoneId.of(ZONE) },
    bestDateTimePattern = { _, skeleton -> if (skeleton == "EEEdMMM") "EEE d MMM" else "HH:mm" }
)

/**
 * Log medication with a hardware keyboard (BRD D181 (6), EC-UX-50): the list, first use and
 * logged orders with their reverse, the dark room's same order, a keyboard log that leaves nothing
 * focused, and the time picker that gives focus back to Taken at.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h1400dp")
class LogMedicationKeyboardOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private var log by mutableStateOf(LOG)
    private var logged by mutableStateOf<IntakeUiModel?>(null)

    private fun show(darkRoom: Boolean = false) = rule.setContent {
        MigrelyTheme {
            LogMedicationContents(
                log = log,
                shownTime = TIME,
                logged = logged,
                problem = null,
                pending = false,
                darkRoom = darkRoom,
                timeUiFormatter = FORMATTER,
                actions = LogMedicationActions(
                    onBack = {},
                    onTimePicked = {},
                    onMedication = {
                        logged = IntakeUiModel("x", it.name, it.defaultDose, TIME, ZONE, "a")
                    },
                    onAddAndLog = { _, _ -> },
                    onUndo = {},
                    onDone = {},
                    onManage = {}
                ),
                messages = {}
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
    fun `The list runs Taken at, each medication, Manage, and reverses`() {
        show()
        assertOrderAndReverse(LIST_ORDER)
    }

    @Test
    fun `The dark room keeps the list's order`() {
        show(darkRoom = true)
        assertOrderAndReverse(LIST_ORDER)
    }

    /** Negative control (task 5.2): an expected list with one stop dropped does not match. */
    @Test
    fun `The order check fails when a stop is missing`() {
        show()
        focusOnBack()

        assertNotEquals(LIST_ORDER - SUMATRIPTAN, rule.walk(LIST_ORDER.size))
    }

    @Test
    fun `First use runs Taken at, Name, the four types, Add and log`() {
        log = LOG.copy(medications = emptyList())
        show()
        focusOnBack()

        assertEquals(FIRST_USE_ORDER, rule.walk(FIRST_USE_ORDER.size))
    }

    @Test
    fun `Logged runs Back, Done, Undo`() {
        logged = IntakeUiModel("x", "Ibuprofen", "400 mg", TIME, ZONE, "a")
        show()
        focusOnBack()

        assertEquals(listOf("Done", "Undo", BACK), rule.walk(3))
    }

    @Test
    fun `A log by Enter leaves nothing focused, and the next Tabs go Back, Done, Undo`() {
        show()
        focusOnBack()
        rule.walk(2)

        rule.press(Key.Enter)

        assertEquals(true, logged != null)
        assertNull(rule.focusedName())
        assertEquals(listOf(BACK, "Done", "Undo"), rule.walk(3))
    }

    @Test
    fun `The time picker gives focus back to Taken at`() {
        show()
        focusOnBack()
        rule.walk(1)

        rule.press(Key.Enter)
        // Past the dialog's guard against a tap meant for the screen below.
        rule.mainClock.advanceTimeBy(DIALOG_WAIT_MILLIS + 100)
        rule.onNodeWithText("Cancel").performClick()
        rule.waitForIdle()

        assertEquals(TAKEN_AT, rule.focusedName())
    }
}
