package com.migrely.app.history.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * History has no text field, so it ignores the keyboard (BRD D180 (3), EC-UX-42, #145): with a 250 dp keyboard inset,
 * open or left behind by Android 8 after a copy, its list ends where it did, in portrait and in landscape.
 */
@RunWith(RobolectricTestRunner::class)
class HistoryKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun assertTheKeyboardChangesNothing() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                HistoryContents(
                    viewState = HistoryViewStateUiModel.Ready(previewMonth(), null),
                    selectedEpochDay = null,
                    mappers = previewMappers,
                    actions = previewActions,
                    messages = {},
                    overuseIndicator = {}
                )
            }
        }
        rule.waitForIdle()
        val before = rule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().map {
            it.boundsInRoot
        }

        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()

        assertEquals(
            before,
            rule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().map {
                it.boundsInRoot
            }
        )
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `In portrait a keyboard inset leaves the list where it was`() =
        assertTheKeyboardChangesNothing()

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `In landscape a keyboard inset leaves both columns where they were`() =
        assertTheKeyboardChangesNothing()
}
