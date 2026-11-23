package com.migrely.app.designsystem.layout

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.migrely.app.designsystem.dialog.MigrelyAlertDialog
import com.migrely.app.designsystem.input.drawEdgeToEdge
import com.migrely.app.designsystem.snackbar.LocalMessageCover
import com.migrely.app.designsystem.snackbar.MessageCover
import com.migrely.app.designsystem.snackbar.MessageLayout
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.test.talkback.TalkBackTree
import com.migrely.app.test.talkback.setNavigationBar
import com.migrely.app.test.talkback.turnOnTalkBack
import kotlin.math.abs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// A gesture navigation bar, so the visible part of a page padded for it ends above the window.
private const val NAVIGATION_BAR_DP = 24

// Page above and below the item, so it can sit anywhere against either edge.
private const val LEAD_DP = 2000
private const val ITEM_DP = 40
private const val ITEM = "item"

// Well past a message's exit, so nothing of it is left.
private const val MESSAGE_GONE_MILLIS = 5_000L

// Within Compose's widening of a clipped child's touch bounds: half of a 48 dp target.
private val HIDDEN_DISTANCES_DP = listOf(1, 12, 23)

/**
 * The one scroll entry point of every page and dialog (#255, BRD D193, design D1 to D4a): an item outside the visible
 * part is not in TalkBack's tree, TalkBack's scroll moves half of what shows, nothing but the minimum touch target
 * changes, and only inside the scroll.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class PageScrollTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var scrollState: ScrollState

    private val navigationBarPx get() = with(rule.density) { NAVIGATION_BAR_DP.dp.roundToPx() }

    private fun show(content: @Composable () -> Unit) {
        rule.runOnUiThread {
            rule.activity.drawEdgeToEdge()
            rule.activity.turnOnTalkBack()
        }
        rule.setContent {
            scrollState = rememberScrollState()
            content()
        }
        rule.runOnUiThread { rule.activity.setNavigationBar(NAVIGATION_BAR_DP) }
        rule.waitForIdle()
    }

    /** A page padded for the navigation bar before its scroll, so its visible part is its viewport. */
    @Composable
    private fun PaddedPage() {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            PageScroll {
                Column(Modifier.fillMaxWidth().weight(1f).pageScroll(scrollState)) {
                    Spacer(Modifier.height(LEAD_DP.dp))
                    Box(Modifier.size(200.dp, ITEM_DP.dp).testTag(ITEM).clickable {})
                    Spacer(Modifier.height(LEAD_DP.dp))
                }
            }
        }
    }

    /** A page that scrolls under the navigation bar, padded for it inside its scroll content. */
    @Composable
    private fun InsetInsidePage() {
        PageScroll {
            Column(
                Modifier
                    .fillMaxSize()
                    .pageScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Spacer(Modifier.height(LEAD_DP.dp))
                Spacer(Modifier.height(LEAD_DP.dp))
            }
        }
    }

    private fun scrollTo(px: Int) {
        rule.runOnIdle { runBlocking { scrollState.scrollTo(px) } }
        rule.waitForIdle()
    }

    private fun itemInTree(): Boolean {
        val id = rule.onNodeWithTag(ITEM).fetchSemanticsNode().id
        var inTree = false
        rule.runOnUiThread { inTree = TalkBackTree(rule.activity).node(id) != null }
        return inTree
    }

    private fun px(dp: Int) = with(rule.density) { dp.dp.roundToPx() }

    @Test
    fun `An item 1 to 23 dp below the visible part, above the bar, is not in the tree`() {
        show { PaddedPage() }
        val viewport = scrollState.viewportSize
        val inTree = HIDDEN_DISTANCES_DP.filter { distance ->
            // The item's top lies this far below the visible part's bottom.
            scrollTo(px(LEAD_DP) - viewport - px(distance))
            itemInTree()
        }
        assertEquals(emptyList<Int>(), inTree)
    }

    @Test
    fun `An item 1 to 23 dp above the visible part is not in the tree`() {
        show { PaddedPage() }
        val inTree = HIDDEN_DISTANCES_DP.filter { distance ->
            scrollTo(px(LEAD_DP + ITEM_DP) + px(distance))
            itemInTree()
        }
        assertEquals(emptyList<Int>(), inTree)
    }

    @Test
    fun `An item with 1 px in view is in the tree`() {
        show { PaddedPage() }
        scrollTo(px(LEAD_DP) - scrollState.viewportSize + 1)
        assertTrue(itemInTree())
    }

    private fun accessibilityScrollForward(): Int {
        val before = scrollState.value
        val page = rule.onNode(hasScrollAction()).fetchSemanticsNode().id
        rule.runOnUiThread {
            TalkBackTree(rule.activity)
                .perform(page, AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
        }
        rule.waitForIdle()
        return scrollState.value - before
    }

    private fun assertWithinOnePx(expected: Int, actual: Int) =
        assertTrue("moved $actual px, expected $expected", abs(expected - actual) <= 1)

    @Test
    fun `One accessibility scroll moves half the visible part, which is the viewport here`() {
        show { PaddedPage() }
        assertWithinOnePx(scrollState.viewportSize / 2, accessibilityScrollForward())
    }

    @Test
    fun `One accessibility scroll moves half of what shows when the inset is inside the scroll`() {
        show { InsetInsidePage() }
        val shows = scrollState.viewportSize - navigationBarPx
        assertWithinOnePx(shows / 2, accessibilityScrollForward())
    }

    @Test
    fun `Inside the scroll only the minimum touch target changes`() {
        val outer = OddViewConfiguration
        var inner: ViewConfiguration? = null
        show {
            CompositionLocalProvider(LocalViewConfiguration provides outer) {
                PageScroll {
                    Column(Modifier.pageScroll(scrollState)) {
                        inner = LocalViewConfiguration.current
                    }
                }
            }
        }
        assertNotNull(inner)
        val inside = inner!!
        assertEquals(
            listOf(
                outer.longPressTimeoutMillis,
                outer.doubleTapTimeoutMillis,
                outer.doubleTapMinTimeMillis,
                outer.touchSlop,
                outer.handwritingSlop,
                outer.maximumFlingVelocity,
                outer.minimumFlingVelocity,
                outer.handwritingGestureLineMargin,
                DpSize.Zero
            ),
            listOf(
                inside.longPressTimeoutMillis,
                inside.doubleTapTimeoutMillis,
                inside.doubleTapMinTimeMillis,
                inside.touchSlop,
                inside.handwritingSlop,
                inside.maximumFlingVelocity,
                inside.minimumFlingVelocity,
                inside.handwritingGestureLineMargin,
                inside.minimumTouchTargetSize
            )
        )
    }

    @Test
    fun `Top bar, message host and dialog buttons keep 48 dp, the scroll has none`() {
        val read = mutableMapOf<String, DpSize>()

        @Composable
        fun Probe(place: String) {
            read[place] = LocalViewConfiguration.current.minimumTouchTargetSize
        }
        show {
            Box(Modifier.fillMaxSize()) {
                Column {
                    Probe("top bar")
                    PageScroll {
                        Column(Modifier.weight(1f).pageScroll(scrollState)) { Probe("page") }
                    }
                }
                Probe("message host")
            }
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    Probe("dialog button")
                    TextButton(onClick = {}) { Text("OK") }
                },
                text = {
                    PageScroll {
                        Column(Modifier.pageScroll(rememberScrollState())) {
                            Probe("dialog content")
                        }
                    }
                }
            )
        }
        val target = DpSize(48.dp, 48.dp)
        assertEquals(
            mapOf(
                "top bar" to target,
                "page" to DpSize.Zero,
                "message host" to target,
                "dialog button" to target,
                "dialog content" to DpSize.Zero
            ),
            read.toMap()
        )
    }

    @Test
    fun `Step only, for Material's pickers, keeps the touch target and the half step`() {
        var inside: ViewConfiguration? = null
        show {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
                PageScroll(exactBounds = false) {
                    Column(Modifier.fillMaxWidth().weight(1f).pageScroll(scrollState)) {
                        inside = LocalViewConfiguration.current
                        Spacer(Modifier.height(LEAD_DP.dp))
                        Spacer(Modifier.height(LEAD_DP.dp))
                    }
                }
            }
        }
        assertEquals(DpSize(48.dp, 48.dp), inside!!.minimumTouchTargetSize)
        assertWithinOnePx(scrollState.viewportSize / 2, accessibilityScrollForward())
    }

    @Test
    fun `One accessibility scroll leaves out a message shown over the page`() {
        var cover: MessageCover? = null
        show {
            MessageLayout(
                message = {
                    MigrelySnackbarHost(message = "Couldn't save.", reduceMotion = true) {
                        MigrelySnackbar(it, SnackbarTone.Error, null, null, "Dismiss", {})
                    }
                }
            ) {
                cover = LocalMessageCover.current
                PaddedPage()
            }
        }
        val message = cover?.bounds
        assertNotNull("the host says where its message is", message)
        assertTrue("the message lies over the page's top", message!!.bottom > 0f)
        val shows = scrollState.viewportSize - message.bottom.toInt()
        assertWithinOnePx(shows / 2, accessibilityScrollForward())
    }

    @Test
    fun `Once the message has gone the step counts the whole page again`() {
        var message by mutableStateOf<String?>("Couldn't save.")
        show {
            MessageLayout(
                message = {
                    MigrelySnackbarHost(message = message, reduceMotion = true) {
                        MigrelySnackbar(it, SnackbarTone.Error, null, null, "Dismiss", {})
                    }
                }
            ) { PaddedPage() }
        }
        rule.runOnIdle { message = null }
        rule.mainClock.advanceTimeBy(MESSAGE_GONE_MILLIS)
        rule.waitForIdle()
        assertWithinOnePx(scrollState.viewportSize / 2, accessibilityScrollForward())
    }
}

/** Values no device has, so a member that does not come from the configuration around the scroll shows. */
private object OddViewConfiguration : ViewConfiguration {
    override val longPressTimeoutMillis = 777L
    override val doubleTapTimeoutMillis = 333L
    override val doubleTapMinTimeMillis = 44L
    override val touchSlop = 9.5f
    override val handwritingSlop = 3.5f
    override val maximumFlingVelocity = 1234f
    override val minimumFlingVelocity = 56f
    override val handwritingGestureLineMargin = 17f
    override val minimumTouchTargetSize = DpSize(48.dp, 48.dp)
}
