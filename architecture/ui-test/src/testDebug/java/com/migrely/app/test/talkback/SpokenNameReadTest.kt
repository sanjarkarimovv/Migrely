package com.migrely.app.test.talkback

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val CHIP = "chip"

/**
 * Why the name TalkBack says is read from the unmerged tree (#255, design D7): a chip cut by the visible edge, with
 * exact bounds, keeps its own node in the tree, but its text child lies outside the visible part and leaves it.
 * TalkBack then says only the chip's state. The merged read still finds the text and can never fail this way.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class SpokenNameReadTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var scrollState: ScrollState

    private fun showChipScrolledBy(scrollDp: Int) {
        rule.runOnUiThread { rule.activity.turnOnTalkBack() }
        rule.setContent {
            scrollState = rememberScrollState()
            val base = LocalViewConfiguration.current
            val exact = object : ViewConfiguration by base {
                override val minimumTouchTargetSize = DpSize.Zero
            }
            CompositionLocalProvider(LocalViewConfiguration provides exact) {
                // The page shows 100 dp; the chip starts 200 dp down, its text 14 dp inside it.
                Column(Modifier.height(100.dp).verticalScroll(scrollState)) {
                    Spacer(Modifier.height(200.dp))
                    Box(
                        Modifier
                            .testTag(CHIP)
                            .selectable(selected = false, onClick = {})
                            .padding(vertical = 14.dp)
                    ) {
                        BasicText("Nausea")
                    }
                    Spacer(Modifier.height(200.dp))
                }
            }
        }
        val px = with(rule.density) { scrollDp.dp.roundToPx() }
        rule.runOnIdle { runBlocking { scrollState.scrollTo(px) } }
        rule.waitForIdle()
    }

    private fun spokenName(): String {
        val chip = rule.onNodeWithTag(CHIP, useUnmergedTree = true).fetchSemanticsNode()
        var name = ""
        rule.runOnUiThread {
            val tree = TalkBackTree(rule.activity)
            assertNotNull("the chip is a stop", tree.node(chip.id))
            name = tree.spokenName(chip)
        }
        return name
    }

    @Test
    fun `A whole chip says its words`() {
        showChipScrolledBy(150)
        assertEquals("Nausea", spokenName())
    }

    @Test
    fun `A chip cut below its text says no words, while the merged read still finds them`() {
        // The chip's top 6 dp show at the bottom edge; its text starts 8 dp below that edge.
        showChipScrolledBy(106)
        assertEquals("", spokenName())
        assertEquals("Nausea", rule.onNodeWithTag(CHIP).fetchSemanticsNode().mergedWords())
    }
}
