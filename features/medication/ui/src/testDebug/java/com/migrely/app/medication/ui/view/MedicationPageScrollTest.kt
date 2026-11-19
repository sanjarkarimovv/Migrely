package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ZONE = "Asia/Tashkent"
private const val TIME = 1_792_829_200_000L

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
 * Each field on Log and Edit medication is scrolled by the page's own scroll, with no other scroll or nested-scroll
 * node between them (BRD D194; #259).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MedicationPageScrollTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun assertEachFieldScrollsWithThePage() {
        rule.waitForIdle()
        val page = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().forEach { field ->
            assertNull(field.inTheWayOfThePage(page))
        }
    }

    @Test
    fun `Log medication's name field's nearest nested-scroll parent is the page`() {
        rule.setContent {
            MigrelyTheme {
                LogMedicationContents(
                    log = FIRST_USE,
                    shownTime = TIME,
                    logged = null,
                    problem = null,
                    pending = false,
                    darkRoom = false,
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
        rule.onNode(hasSetTextAction() and hasText("Name")).assertExists()

        assertEachFieldScrollsWithThePage()
    }

    @Test
    fun `Edit medication's fields' nearest nested-scroll parent is the page`() {
        rule.setContent {
            MigrelyTheme {
                EditMedicationContents(
                    adding = false,
                    notFound = false,
                    form = MedicationFormUiModel(
                        name = "Ibuprofen",
                        kind = MedicationKindUiModel.Acute
                    ),
                    problem = null,
                    pending = false,
                    onFormChange = {},
                    onSave = {},
                    onBack = {},
                    messages = {}
                )
            }
        }

        assertEachFieldScrollsWithThePage()
    }
}
