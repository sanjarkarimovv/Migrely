package com.migrely.app.designsystem.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.getScrollViewportLength
import androidx.compose.ui.unit.DpSize
import kotlin.math.floor

/**
 * The one way a Migrely page or dialog scrolls vertically (#255, BRD D193). Wrap only the scrolling layout, never the
 * top bar, the message host or a dialog's buttons, and give that layout [PageScrollScope.pageScroll] in place of
 * `verticalScroll`:
 *
 * ```
 * PageScroll {
 *     Column(Modifier.windowInsetsPadding(insets).pageScroll(scrollState)) { … }
 * }
 * ```
 *
 * Inside, an item outside the visible part leaves TalkBack's tree instead of staying in it at the top of its order:
 * Compose otherwise keeps a node lying up to half a minimum touch target (24 dp) outside a scroll's clip, with empty
 * visible bounds. So the minimum touch target is zero inside, and every other part of the view configuration (double
 * tap and long press timeouts, touch slop) is the one around it. Compose's pointer input then no longer widens a
 * clickable smaller than 48 dp inside the page: each page's controls are measured against that (design D5).
 *
 * TalkBack's (and Switch Access's) scroll action moves half of what shows, so the item TalkBack is on stays on screen
 * and it keeps its place, where a whole page made it skip a block (see [PageScrollScope.pageScroll]).
 */
@Composable
fun PageScroll(content: @Composable PageScrollScope.() -> Unit) {
    val around = LocalViewConfiguration.current
    val exact = remember(around) { ExactBoundsViewConfiguration(around) }
    val obscuring = MigrelyInsets.barsAndKeyboard
    val scope = remember(obscuring) { PageScrollScopeImpl(obscuring) }
    CompositionLocalProvider(LocalViewConfiguration provides exact) { scope.content() }
}

/** Where [pageScroll] can be called: only inside [PageScroll], which gives it exact bounds. */
@Stable
interface PageScrollScope {
    /**
     * `verticalScroll(state)`, with TalkBack's scroll step set to floor(V / 2) px: V is the part of this layout's
     * viewport that shows, its height less any system bar, cutout or keyboard over it, so a page whose inset lies
     * inside its scroll content steps by half of what shows, not half of the viewport. Less than a step left scrolls
     * to the end. Finger scroll, fling, Tab and Page Up or Down do not use the step.
     */
    fun Modifier.pageScroll(state: ScrollState): Modifier
}

private class PageScrollScopeImpl(private val obscuring: WindowInsets) : PageScrollScope {
    // The step's node first: on one layout node the outer semantics win, and it measures the scroll's viewport.
    override fun Modifier.pageScroll(state: ScrollState): Modifier =
        this then TalkBackStepElement(obscuring) then Modifier.verticalScroll(state)
}

/** The configuration around the page, with no minimum touch target: nothing else may differ (design D2). */
private class ExactBoundsViewConfiguration(around: ViewConfiguration) :
    ViewConfiguration by around {
    override val minimumTouchTargetSize: DpSize
        get() = DpSize.Zero
}

private data class TalkBackStepElement(val obscuring: WindowInsets) :
    ModifierNodeElement<TalkBackStepNode>() {
    override fun create() = TalkBackStepNode(obscuring)

    override fun update(node: TalkBackStepNode) {
        node.obscuring = obscuring
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "talkBackStep"
    }
}

private class TalkBackStepNode(var obscuring: WindowInsets) :
    Modifier.Node(),
    SemanticsModifierNode,
    LayoutAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var viewport: LayoutCoordinates? = null

    override fun onPlaced(coordinates: LayoutCoordinates) {
        viewport = coordinates
    }

    override fun SemanticsPropertyReceiver.applySemantics() {
        getScrollViewportLength { step() }
    }

    /** Read when TalkBack scrolls, so it follows the keyboard and the bars as they are then; null before layout. */
    private fun step(): Float? {
        val viewport = viewport?.takeIf { it.isAttached } ?: return null
        val density = currentValueOf(LocalDensity)
        val window = viewport.findRootCoordinates().size.height
        val bounds = viewport.boundsInRoot()
        val top = maxOf(bounds.top, obscuring.getTop(density).toFloat())
        val bottom = minOf(bounds.bottom, (window - obscuring.getBottom(density)).toFloat())
        return floor((bottom - top) / 2).coerceAtLeast(1f)
    }
}
