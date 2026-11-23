package com.migrely.app.history.ui.view

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.test.talkback.talkBackWalk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * TalkBack walks every page of this module end to end, each stop once and named, also when cut by an edge (#255,
 * tasks 4.x): its order down each page, its scroll back and on, and the cut sweep. A window drawn edge to edge with
 * a navigation bar, so each page's visible part ends above the window, as on a phone.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryTalkBackWalkTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, TalkBack walks every page`() =
        assertEquals(emptyList<String>(), rule.talkBackWalk(1f, historyScreens))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp tall at 200 percent, TalkBack walks every page`() =
        assertEquals(emptyList<String>(), rule.talkBackWalk(2f, historyScreens))
}
