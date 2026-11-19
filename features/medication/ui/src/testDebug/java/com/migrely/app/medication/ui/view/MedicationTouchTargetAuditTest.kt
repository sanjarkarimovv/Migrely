package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import com.migrely.app.medication.ui.model.MedicationPromptUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import com.migrely.app.medication.ui.model.MedicationsViewStateUiModel
import com.migrely.app.test.talkback.auditTouchTargets
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ZONE = "Asia/Tashkent"
private const val TIME = 1_792_829_200_000L

private val LOG = MedicationLogUiModel(
    defaultTimeEpochMillis = TIME,
    zoneId = ZONE,
    attackId = "a",
    medications = listOf(
        MedicationUiModel("i", "Ibuprofen", MedicationKindUiModel.Acute, null, "400 mg")
    )
)
private val FORMATTER = MedicationTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { ZoneId.of(ZONE) },
    bestDateTimePattern = { _, skeleton -> if (skeleton == "EEEdMMM") "EEE d MMM" else "HH:mm" }
)

/**
 * Touch-target audit of the medication pages and Today's medication card before exact bounds land on them (#255, design D5; lead decision 2026-11-19, option
 * 1): every control inside a page is 48 dp of its own, at 360 and 411 dp wide and 100% and 200% font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MedicationTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "Edit medication" to {
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
        },
        "Log medication" to {
            MigrelyTheme {
                LogMedicationContents(
                    log = LOG,
                    shownTime = TIME,
                    logged = null,
                    problem = null,
                    pending = false,
                    darkRoom = false,
                    timeUiFormatter = FORMATTER,
                    actions = LogMedicationActions({}, {}, {}, { _, _ -> }, {}, {}, {}),
                    messages = {}
                )
            }
        },
        "Log medication, dark room" to {
            MigrelyTheme {
                LogMedicationContents(
                    log = LOG,
                    shownTime = TIME,
                    logged = null,
                    problem = null,
                    pending = false,
                    darkRoom = true,
                    timeUiFormatter = FORMATTER,
                    actions = LogMedicationActions({}, {}, {}, { _, _ -> }, {}, {}, {}),
                    messages = {}
                )
            }
        },
        "Medications" to {
            MigrelyTheme {
                MedicationsContents(
                    viewState = MedicationsViewStateUiModel.Ready(
                        listOf(
                            MedicationUiModel(
                                "i",
                                "Ibuprofen",
                                MedicationKindUiModel.Acute,
                                null,
                                null
                            )
                        )
                    ),
                    actions = MedicationsActions({}, {}, {}, {}),
                    messages = {}
                )
            }
        },
        "Today's effect question" to {
            MigrelyTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MedicationPromptContents(
                        prompt = MedicationPromptUiModel.EffectQuestion(
                            "i",
                            "Ibuprofen",
                            "400 mg",
                            TIME,
                            ZONE
                        ),
                        timeUiFormatter = FORMATTER,
                        actions = MedicationPromptActions({ _, _ -> }, {}, { _, _ -> })
                    )
                }
            }
        }
    )

    private fun underBar(fontScale: Float) = rule.auditTouchTargets(fontScale, screens)

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `360 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `411 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `360 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `411 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))
}
