package com.migrely.app.attack.ui.view

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

/** The ease of the scroll back to the top: Material's standard ease in-out (BRD D167). */
private const val REVEAL_MILLIS = 300

/**
 * Brings the first-10 safety card into the visible content area when it appears (BRD D167, EC-ATK-23; #171). The card
 * is the scrolling column's first child, so the content goes back to its top: the card is then exactly where the
 * unscrolled screen puts it, and a message over a failed save covers no more of it than in 1.1.0 (#174). TalkBack
 * does not speak a live region that is out of view, so the card becomes one only once it is in view: [announces] is
 * false from the card's event until the scroll ends. The scroll holds a touch off while it runs, so it always ends
 * with the card in view, and a restored card does not scroll, because its event does not come again.
 */
@Stable
internal class SafetyCardReveal(private val scrollState: ScrollState, revealedArrivals: Int) {
    private var viewport: LayoutCoordinates? = null
    private var card: LayoutCoordinates? = null
    private var revealedArrivals by mutableIntStateOf(revealedArrivals)

    /** On the scrolling column, before its scroll: the visible content area. */
    val viewportModifier: Modifier = Modifier.onGloballyPositioned { viewport = it }

    /** On the card itself. */
    val cardModifier: Modifier = Modifier.onGloballyPositioned { card = it }

    /** Whether the card of the [arrivals]th event may be a live region yet. */
    fun announces(arrivals: Int): Boolean = revealedArrivals >= arrivals

    /**
     * After the card's [arrivals]th event: once it is laid out, scrolls the content back to its top when the card is not
     * fully inside, easing or, with [reduceMotion], at once; then lets TalkBack speak it.
     */
    suspend fun reveal(arrivals: Int, reduceMotion: Boolean) {
        withFrameNanos { }
        val distance = if (cardFullyInside()) 0f else -scrollState.value.toFloat()
        if (distance != 0f) {
            scrollState.scroll(MutatePriority.PreventUserInput) {
                if (reduceMotion) {
                    scrollBy(distance)
                } else {
                    var scrolled = 0f
                    val ease = tween<Float>(REVEAL_MILLIS, easing = FastOutSlowInEasing)
                    animate(0f, distance, animationSpec = ease) { value, _ ->
                        scrolled += scrollBy(value - scrolled)
                    }
                }
            }
        }
        revealedArrivals = arrivals
    }

    /** Whether the card lies fully inside the area; true when either is not laid out, so nothing moves. */
    private fun cardFullyInside(): Boolean {
        val area = viewport?.takeIf { it.isAttached } ?: return true
        val shown = card?.takeIf { it.isAttached } ?: return true
        val top = area.localPositionOf(shown, Offset.Zero).y
        return top >= 0f && top + shown.size.height <= area.size.height
    }
}

@Composable
internal fun rememberSafetyCardReveal(scrollState: ScrollState, arrivals: Int): SafetyCardReveal =
    remember(scrollState) { SafetyCardReveal(scrollState, arrivals) }
