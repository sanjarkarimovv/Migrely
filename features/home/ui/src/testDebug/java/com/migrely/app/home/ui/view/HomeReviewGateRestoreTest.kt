package com.migrely.app.home.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val REVIEW = "review"

/** The review gate survives a rotation and a restored process (BRD D178 (3), ux F3: "across rotation"). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HomeReviewGateRestoreTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var gate: ReviewGate

    private fun showGate(): StateRestorationTester {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            gate = rememberReviewGate(rememberNavController())
            if (gate.open) Text(REVIEW)
        }
        rule.waitForIdle()
        return restoration
    }

    @Test
    fun `After an error this visit the slot stays gone across a restore`() {
        val restoration = showGate()
        rule.runOnIdle {
            gate.onErrorPosted()
            gate.onShownChanged(false)
        }

        restoration.emulateSavedInstanceStateRestore()

        rule.onNodeWithText(REVIEW).assertDoesNotExist()
    }

    @Test
    fun `With a message on screen the slot stays gone across a restore`() {
        val restoration = showGate()
        rule.runOnIdle { gate.onShownChanged(true) }

        restoration.emulateSavedInstanceStateRestore()

        rule.onNodeWithText(REVIEW).assertDoesNotExist()
    }

    @Test
    fun `With neither the slot is there after a restore`() {
        val restoration = showGate()

        restoration.emulateSavedInstanceStateRestore()

        rule.onNodeWithText(REVIEW).assertExists()
    }
}
