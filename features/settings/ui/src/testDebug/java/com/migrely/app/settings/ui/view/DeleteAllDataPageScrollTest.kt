package com.migrely.app.settings.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The confirmation word's field is scrolled by the page's own scroll, with no other scroll or nested-scroll node
 * between them (BRD D194; #259).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DeleteAllDataPageScrollTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `The word's nearest nested-scroll parent is the page`() {
        rule.setContent {
            MigrelyTheme {
                DeleteAllDataContents(
                    typed = "",
                    word = "DELETE",
                    confirmed = false,
                    deleting = false,
                    failed = false,
                    actions = DeleteAllDataActions({}, {}, {})
                )
            }
        }
        rule.waitForIdle()

        val field = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        val page = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        assertNull(field.inTheWayOfThePage(page))
    }
}
