package com.migrely.app.designsystem.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// The page's visible part, in dp from the root's top: 100 to 300.
private val PAGE_TOP = 100.dp
private val PAGE_HEIGHT = 200.dp

/**
 * How far the page must scroll for an area to show in its visible part (BRD D194, design D2; task 3.1): 0 where it
 * shows, the distance below or above, its top first where it is taller than the part, null while the page is unknown or
 * the field is detached. Density 1, so dp are px.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp-mdpi")
class TypingSpaceScrollToShowTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var space: TypingSpace
    private lateinit var field: LayoutCoordinates
    private var withPage by mutableStateOf(true)
    private var withField by mutableStateOf(true)

    /** A page at 100..300 and a field whose top is at 0 in the root, so an area's numbers are root numbers. */
    private fun show(page: Boolean = true) {
        withPage = page
        rule.setContent {
            space = rememberTypingSpace()
            Box {
                if (withPage) {
                    Box(
                        Modifier
                            .offset(y = PAGE_TOP)
                            .fillMaxWidth()
                            .height(PAGE_HEIGHT)
                            .then(space.pageModifier)
                    )
                }
                if (withField) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .onGloballyPositioned { field = it }
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun area(top: Float, bottom: Float) = Rect(0f, top, 10f, bottom)

    private fun scrollToShow(top: Float, bottom: Float) = rule.runOnIdle {
        space.scrollToShow(field, area(top, bottom))
    }

    @Test
    fun `An area inside the visible part needs no scroll`() {
        show()

        assertEquals(0f, scrollToShow(150f, 250f))
    }

    @Test
    fun `An area below the visible part scrolls by what is below`() {
        show()

        assertEquals(40f, scrollToShow(250f, 340f))
    }

    @Test
    fun `An area above the visible part scrolls back by what is above`() {
        show()

        assertEquals(-30f, scrollToShow(70f, 150f))
    }

    @Test
    fun `An area taller than the visible part shows its top first`() {
        show()

        assertEquals(150f, scrollToShow(250f, 500f))
    }

    @Test
    fun `With no page laid out there is nothing to measure against`() {
        show(page = false)

        assertNull(scrollToShow(250f, 340f))
    }

    @Test
    fun `A field no longer laid out gives no distance`() {
        show()
        val detached = field
        withField = false
        rule.waitForIdle()

        assertNull(rule.runOnIdle { space.scrollToShow(detached, area(250f, 340f)) })
    }
}
