package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The note hands its keyboard follow to the page's own scroll, with no other nested-scroll node between them (BRD D194,
 * design D3; #259): that page takes the distance in the keyboard's turn.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AttackDetailsPageScrollTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `The note's nearest nested-scroll parent is the page`() {
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(
                        previewDetails().copy(note = "")
                    ),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = false,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {},
                        noteDraft = null,
                        onNoteDraft = {}
                    ),
                    actions = previewActions
                )
            }
        }
        rule.waitForIdle()

        val note = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        val page = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        assertNull(note.inTheWayOfThePage(page))
    }
}
