package com.migrely.app.attack.ui.view

import android.app.Application
import android.view.WindowManager
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

/**
 * In a short window the pain and effect dialogs take the wide width, so their buttons do not stack over the content
 * (T23 8.1 row R04, cr-de round 2 F6 (b)); in a tall window they keep the platform's.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h360dp-land")
class AttackDialogWidthTest {
    @get:Rule
    val rule = createComposeRule()

    private val details = previewDetails().copy(
        intakes = listOf(
            AttackIntakeUiModel(
                id = "i",
                medicationId = "m",
                medicationName = "Ibuprofen",
                dose = "400 mg",
                takenAtEpochMillis = previewDetails().startEpochMillis,
                zoneId = previewDetails().startZoneId
            )
        )
    )

    private fun show() = rule.setContent {
        MigrelyTheme {
            AttackDetailsContents(
                viewState = AttackDetailsViewStateUiModel.Ready(details),
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

    /**
     * Whether the dialog window takes the wide width: without the platform's default width, Compose lays the window
     * out at MATCH_PARENT; with it, at WRAP_CONTENT.
     */
    private fun wide(): Boolean {
        rule.waitForIdle()
        return ShadowDialog.getLatestDialog().window!!.attributes.width ==
            WindowManager.LayoutParams.MATCH_PARENT
    }

    private fun openPain() {
        rule.onNodeWithText("Add pain level").performScrollTo().performClick()
    }

    private fun openEffect() {
        rule.onNode(
            hasContentDescription("Ibuprofen 400 mg, ", substring = true) and hasClickAction()
        )
            .performScrollTo().performClick()
    }

    @Test
    fun `Given a short window then the pain dialog is wide`() {
        show()
        openPain()

        assertTrue(wide())
    }

    @Test
    fun `Given a short window then the effect dialog is wide`() {
        show()
        openEffect()

        assertTrue(wide())
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-port")
    fun `Given a tall window then the pain dialog keeps the platform width`() {
        show()
        openPain()

        assertFalse(wide())
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-port")
    fun `Given a tall window then the effect dialog keeps the platform width`() {
        show()
        openEffect()

        assertFalse(wide())
    }
}
