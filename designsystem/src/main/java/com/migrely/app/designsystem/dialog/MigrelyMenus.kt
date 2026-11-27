package com.migrely.app.designsystem.dialog

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/** The room kept between the menu and the window's safe edges (BRD EC-UX-91). */
private val SAFE_EDGE_MARGIN = 8.dp

/** Material's menu card: its column's padding, and its opening scale and fade (Material 1.4.0 `DropdownMenu`). */
private val MENU_VERTICAL_PADDING = 8.dp
private const val CLOSED_SCALE = 0.8f
private const val SCALE_DAMPING = 0.9f
private const val SCALE_STIFFNESS = 1400f
private const val FADE_DAMPING = 1f
private const val FADE_STIFFNESS = 3800f

/**
 * Material's `DropdownMenu`, with two changes.
 *
 * Its place (issue #179, BRD EC-UX-91): it never covers its anchor. It opens below when it fits above the window's
 * safe bottom edge with 8 dp, else above with 8 dp, else on the larger side, capped to it, its content scrolling. The
 * safe edges come from the system bars, the cutout and an open keyboard. Material's own rule falls back to the
 * anchor's middle when the menu fits neither side with 48 dp, so the next tap on the anchor landed on an item. A tap
 * outside, on the anchor too, only closes it: the popup takes focus, as Material's does.
 *
 * A lift on its card counted for the windows beneath, as a lift on a dialog's card is (issue #161, BRD D164). The first
 * tap of a double tap on an item can close the menu, and the second would then land on the screen beneath. The lift
 * modifier is on the card's column, in the popup's own window, as Material puts its `modifier`, so it sees every press
 * on the card, its padding included, before an item does. There is no wait: the menu opens on a long press, with the
 * finger still down, so the person sees it before the next tap. It adds no semantics and draws nothing.
 *
 * The card, its padding, scrolling, scale and fade are Material's, so the menu looks, moves and reads as before, and
 * follows "Remove animations".
 */
@Composable
fun MigrelyDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded
    if (!expandedState.currentState && !expandedState.targetState) return

    val density = LocalDensity.current
    val safeEdges = WindowInsets.systemBars.union(
        WindowInsets.displayCutout
    ).union(WindowInsets.ime)
    val safeTop = safeEdges.getTop(density)
    val safeBottom = LocalWindowInfo.current.containerSize.height - safeEdges.getBottom(density)
    val margin = with(density) { SAFE_EDGE_MARGIN.roundToPx() }
    val anchor = remember { mutableStateOf<IntRect?>(null) }
    val transformOrigin = remember { mutableStateOf(TransformOrigin.Center) }
    val positionProvider = remember(safeTop, safeBottom, margin) {
        MenuPositionProvider(safeTop, safeBottom, margin) { anchorBounds, menuBounds ->
            anchor.value = anchorBounds
            transformOrigin.value = menuTransformOrigin(anchorBounds, menuBounds)
        }
    }
    val maximumHeight = anchor.value?.let {
        MenuRoom.of(it, safeTop, safeBottom, margin).maximumHeight
    }

    Popup(
        onDismissRequest = onDismissRequest,
        popupPositionProvider = positionProvider,
        properties = PopupProperties(focusable = true)
    ) {
        MenuCard(
            expandedState = expandedState,
            transformOrigin = transformOrigin,
            maximumHeightPx = maximumHeight,
            content = content
        )
    }
}

@Composable
private fun MenuCard(
    expandedState: MutableTransitionState<Boolean>,
    transformOrigin: MutableState<TransformOrigin>,
    maximumHeightPx: Int?,
    content: @Composable ColumnScope.() -> Unit
) {
    val transition = rememberTransition(expandedState, label = "MigrelyDropdownMenu")
    val scale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = SCALE_DAMPING, stiffness = SCALE_STIFFNESS) },
        label = "scale"
    ) { expanded -> if (expanded) 1f else CLOSED_SCALE }
    val alpha by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = FADE_DAMPING, stiffness = FADE_STIFFNESS) },
        label = "alpha"
    ) { expanded -> if (expanded) 1f else 0f }
    val heightCap = maximumHeightPx?.let { px ->
        Modifier.heightIn(max = with(LocalDensity.current) { px.toDp() })
    } ?: Modifier
    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            this.transformOrigin = transformOrigin.value
        },
        shape = MenuDefaults.shape,
        color = MenuDefaults.containerColor,
        tonalElevation = MenuDefaults.TonalElevation,
        shadowElevation = MenuDefaults.ShadowElevation
    ) {
        Column(
            modifier = Modifier
                .liftOnCard()
                .then(heightCap)
                .padding(vertical = MENU_VERTICAL_PADDING)
                .width(IntrinsicSize.Max)
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}
