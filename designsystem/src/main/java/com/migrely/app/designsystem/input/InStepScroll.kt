package com.migrely.app.designsystem.input

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LayoutCoordinates
import kotlin.math.abs

/**
 * Moves a focused field's page in the same turn as the keyboard's frame, so the line being typed is in view before the
 * page reports a scroll (BRD D194, #259; design D2, D3). An animated bring left the line under the keyboard while the
 * page reported its first scroll, and TalkBack, finding its focus gone at that moment, moved it to the first item on
 * screen.
 *
 * The distance goes to the page as a nested post-scroll from the field: the page's `verticalScroll` is the field's
 * nearest nested-scroll parent on every screen with a [TypingSpace] and takes it at once. A nested-scroll ancestor
 * between the field and the page sees the distance first and could take it; none does on the four screens, and each
 * screen's tests check that its line shows. The field itself takes no part in its parents' scrolling.
 */
internal class InStepScroll {
    private val page = NestedScrollDispatcher()

    /** Put on the field. */
    val modifier: Modifier = Modifier.nestedScroll(NotScrolling, page)

    /**
     * Scrolls the page now so that the largest of [areas] that fits [space]'s visible part shows, or the last (the
     * smallest) where none fits, as nested bring requests resolve (ux U1). Each area is in [field]'s coordinates, the
     * largest first. False where there is no answer (no typing space, its page not laid out yet, the field detached)
     * or the page did not take the whole distance, as while another scroll of it is still running (a scrollable takes
     * no raw scroll then): the caller then brings the areas into view its own way, which joins that scroll.
     */
    fun show(space: TypingSpace?, field: LayoutCoordinates?, vararg areas: Rect): Boolean {
        val visible = space?.visibleHeight ?: return false
        val area = areas.firstOrNull { it.height <= visible } ?: areas.last()
        val shift = field?.let { space.scrollToShow(it, area) } ?: return false
        if (shift == 0f) return true
        val taken = page.dispatchPostScroll(
            Offset.Zero,
            Offset(0f, -shift),
            NestedScrollSource.SideEffect
        )
        return abs(taken.y + shift) < 1f
    }
}

/** Takes no part in its parents' scrolling: drags inside the field reach the page as before. */
private object NotScrolling : NestedScrollConnection
