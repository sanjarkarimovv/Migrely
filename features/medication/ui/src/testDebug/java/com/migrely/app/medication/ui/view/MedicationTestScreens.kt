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

/** The pages and cards of this module that the #255 tests show: the touch-target audit and TalkBack's walk. */
internal val medicationScreens: Map<String, @Composable () -> Unit> = mapOf(
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
