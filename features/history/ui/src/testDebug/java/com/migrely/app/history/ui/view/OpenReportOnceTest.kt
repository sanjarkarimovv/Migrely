package com.migrely.app.history.ui.view

import android.app.Application
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * History opened by the doctor visit reminder opens the report once (BRD D168, design D6, review 6): not again after a
 * rotation or a process restore. SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OpenReportOnceTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var opened = 0

    @Test
    fun givenOpenedFromTheReminder_whenTheStateIsRestored_thenTheReportOpenedOnce() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { OpenReportOnce(openReport = true) { opened++ } }
        composeTestRule.waitForIdle()

        restoration.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        assertEquals(1, opened)
    }

    @Test
    fun givenOpenedFromHistoryItself_thenTheReportDoesNotOpen() {
        composeTestRule.setContent { OpenReportOnce(openReport = false) { opened++ } }
        composeTestRule.waitForIdle()

        assertEquals(0, opened)
    }
}
