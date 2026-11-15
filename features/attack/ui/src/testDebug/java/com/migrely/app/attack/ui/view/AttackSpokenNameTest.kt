package com.migrely.app.attack.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Attack details' controls whose spoken label differs from their text read the label once (BRD
 * D181 (2), EC-UX-45): the Started and Ended rows, the "+ Your own" chips and the pain dialog's
 * time.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class AttackSpokenNameTest {
    @get:Rule
    val rule = createComposeRule()

    private val details = previewDetails()

    private fun show(shown: AttackDetailsUiModel = details) = rule.setContent {
        MigrelyTheme {
            AttackDetailsContents(
                viewState = AttackDetailsViewStateUiModel.Ready(shown),
                timeUiFormatter = previewTimeFormatter,
                labels = AttackLabels.Default,
                localState = AttackDetailsLocalState(
                    timeError = null,
                    safetyCardVisible = false,
                    onDismissSafetyCard = {},
                    onTimeErrorShown = {},
                    safetyCardArrivals = 0
                ),
                actions = previewActions
            )
        }
    }

    private fun dateTime(epochMillis: Long) =
        previewTimeFormatter.formatDateTime(epochMillis, details.startZoneId)

    @Test
    fun `The Started and Ended rows each have one spoken name`() {
        show()
        val started = dateTime(details.startEpochMillis)
        val ended = dateTime(checkNotNull(details.endEpochMillis))

        rule.assertOneSpokenName(
            spoken = "Change when it started, now $started",
            visible = "Started $started"
        )
        rule.assertOneSpokenName(
            spoken = "Change when it ended, now $ended",
            visible = "Ended $ended"
        )
    }

    @Test
    fun `The two Your own chips each have one spoken name`() {
        show()

        rule.assertOneSpokenName(spoken = "Add your own symptom", visible = "+ Your own")
        rule.assertOneSpokenName(spoken = "Add your own trigger", visible = "+ Your own")
    }

    /** ux-designer's label (2026-11-15): it starts with the visible "At 14:30". */
    @Test
    fun `The pain dialog's time button has one spoken name`() {
        show()
        rule.onNodeWithText("Add pain level").performScrollTo().performClick()
        val at = previewTimeFormatter.formatWithinAttack(
            details.startEpochMillis,
            details.startEpochMillis,
            details.startZoneId
        )

        rule.assertOneSpokenName(spoken = "At $at, change the time", visible = "At $at")
    }

    /** Already read once (D181 (2), ux check list): its texts are cleared, and it stays as it is. */
    @Test
    fun `A medication row reads only its description`() {
        show(
            details.copy(
                intakes = listOf(
                    AttackIntakeUiModel(
                        id = "i",
                        medicationId = "m",
                        medicationName = "Ibuprofen",
                        dose = "400 mg",
                        takenAtEpochMillis = details.startEpochMillis,
                        zoneId = details.startZoneId
                    )
                )
            )
        )

        val row = rule.onNode(
            hasContentDescription("Ibuprofen 400 mg, ", substring = true) and hasClickAction()
        )
        assertNull(row.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text))
    }
}
