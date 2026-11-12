package com.migrely.app.attack.ui.view

import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TITLE = "Sudden, extremely bad pain?"
private const val DELETE = "Delete migraine"

private val ANNOUNCED =
    SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive)
private val SILENT = SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion)

/** The card's own top padding: its place in the unscrolled column. */
private val NATURAL_TOP = 8.dp

/**
 * The first-10 safety card comes into view when it appears, and only then is a live region (BRD D167, EC-ATK-23;
 * #171).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class SafetyCardRevealTest {
    @get:Rule
    val rule = createComposeRule()

    private var cardVisible by mutableStateOf(false)
    private var arrivals by mutableIntStateOf(0)

    private val content get() = rule.onNode(hasScrollAction())
    private val card: SemanticsNodeInteraction get() = rule.onNodeWithText(TITLE).onParent()

    private fun show() {
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = cardVisible,
                        onDismissSafetyCard = { cardVisible = false },
                        onTimeErrorShown = {},
                        safetyCardArrivals = arrivals
                    ),
                    actions = previewActions
                )
            }
        }
    }

    /** The first 10 is recorded: the card shows, as its event does on the screen. */
    private fun firstTen() {
        cardVisible = true
        arrivals++
        // With the clock held, the composition sees the change at the next frame, as on a device.
        Snapshot.sendApplyNotifications()
    }

    private fun scrollDownToDelete() = content.performScrollToNode(hasText(DELETE))

    private fun scrollValue() =
        content.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun cardTopFromContentTop() =
        card.getUnclippedBoundsInRoot().top - content.getUnclippedBoundsInRoot().top

    /**
     * The content is back at its top, so the card is where the unscrolled layout puts it: the column's first child, under
     * its own 8 dp. The message on a failed save then covers only what it covers in 1.1.0 (#174).
     */
    private fun assertCardAtNaturalPlace() {
        assertEquals(0f, scrollValue())
        assertEquals(NATURAL_TOP.value, cardTopFromContentTop().value, 0.5f)
    }

    private fun animationsRemoved(removed: Boolean) = Settings.Global.putFloat(
        ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        if (removed) 0f else 1f
    )

    @Test
    fun `Given the content is scrolled down when the card appears then it goes to its top`() {
        show()
        scrollDownToDelete()

        firstTen()
        rule.mainClock.advanceTimeBy(1_000)

        assertCardAtNaturalPlace()
        card.assert(ANNOUNCED)
    }

    @Test
    fun `Given the card appears in view then nothing scrolls and it is announced at once`() {
        show()
        val before = scrollValue()

        firstTen()
        rule.waitForIdle()

        assertEquals(before, scrollValue())
        // The unscrolled layout, which a reveal must reproduce.
        assertEquals(NATURAL_TOP.value, cardTopFromContentTop().value, 0.5f)
        card.assert(ANNOUNCED)
    }

    @Test
    fun `Given the card is out of view then it is no live region until the scroll ends`() {
        show()
        scrollDownToDelete()
        rule.mainClock.autoAdvance = false

        firstTen()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()

        card.assert(SILENT)
        rule.mainClock.advanceTimeBy(1_000)
        card.assert(ANNOUNCED)
        assertCardAtNaturalPlace()
    }

    @Test
    fun `Given animations on then the content eases to the card over several frames`() {
        animationsRemoved(false)
        show()
        scrollDownToDelete()
        rule.mainClock.autoAdvance = false

        firstTen()
        repeat(3) { rule.mainClock.advanceTimeByFrame() }
        assertTrue("mid-scroll", cardTopFromContentTop() < (-1).dp)
        rule.mainClock.advanceTimeBy(1_000)
        assertCardAtNaturalPlace()
    }

    @Test
    fun `Given Remove animations then the content jumps to the card`() {
        animationsRemoved(true)
        try {
            show()
            scrollDownToDelete()
            rule.mainClock.autoAdvance = false

            firstTen()
            repeat(3) { rule.mainClock.advanceTimeByFrame() }

            assertCardAtNaturalPlace()
        } finally {
            animationsRemoved(false)
        }
    }

    @Test
    fun `Given a drag during the scroll then the scroll still ends with the card in view`() {
        show()
        scrollDownToDelete()
        rule.mainClock.autoAdvance = false

        firstTen()
        repeat(3) { rule.mainClock.advanceTimeByFrame() }
        content.performTouchInput { swipeUp() }
        rule.mainClock.advanceTimeBy(1_000)

        assertCardAtNaturalPlace()
    }
}
