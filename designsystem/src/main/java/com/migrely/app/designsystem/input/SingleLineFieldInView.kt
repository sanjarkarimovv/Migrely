package com.migrely.app.designsystem.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** The clearance between the input line and the scrolling area's edges, the keyboard's top included (ux U1). */
private val LINE_MARGIN = 8.dp

// Material's body text line, for a style that gives none.
private val BODY_LINE = 24.sp

/**
 * A single-line outlined [field] that its scrolling parent keeps in view while it has focus, by ux U1's priority (BRD
 * D180 (2), EC-UX-40): the whole field with its supporting line when it fits; if not, its input line with the
 * supporting line below; if not, the input line alone, [margin] clear of the visible area's edges. It acts on focus,
 * when the field's size changes, as when an error line appears, and when the keyboard grows while the input line is in
 * view (KeyboardFollow): a closing keyboard, or one that grows after the person scrolled away, never moves the page.
 *
 * Pass the given modifier first in the field's chain and the given interaction source to it. [hasLabel] says where the
 * input line sits: below half a floating label's line, as Material places it, and its content padding.
 *
 * Each new value of [inViewRequests] above 0 brings the field into view the same way without focus, once the frame
 * after the request is laid out, so a supporting line that has just appeared is included (BRD D191, ux F2). Inside a
 * [TypingSpace] it keeps the same reduced margins as a focused field (BRD D192).
 *
 * The input line's height is the field's line as laid out in [LocalTextStyle], the style the field uses (BRD D192): not
 * that style's line height in sp, which nonlinear font scaling converts to less (95 against 112 px at 200%). The same
 * height gives [inputLineRoom]. A field that passes its own `textStyle` must provide it as [LocalTextStyle] around this.
 */
@Composable
fun SingleLineFieldInView(
    hasLabel: Boolean,
    margin: Dp = LINE_MARGIN,
    inViewRequests: Int = 0,
    field: @Composable (modifier: Modifier, interactionSource: MutableInteractionSource) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val requester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val placed = remember { Placed() }
    val follow = remember { KeyboardFollow() }
    val density = LocalDensity.current
    // The insets object, not its value: the keyboard's frames do not recompose the screen.
    val keyboard = WindowInsets.ime
    val labelLine = MaterialTheme.typography.bodySmall.lineHeight.orBodyLine()
    val labelHalf = if (hasLabel) labelLine / 2 else 0.sp
    val lineTop = with(density) {
        labelHalf.toPx() + OutlinedTextFieldDefaults.contentPadding().calculateTopPadding().toPx()
    }
    val lineHeight = inputLineHeight()
    val marginPx = with(density) { margin.toPx() }
    val space = LocalTypingSpace.current

    // ux U1's areas, the largest first: the whole field, then from the input line down, then the input line, which
    // keeps what margin the visible part, [visible] px tall where given, leaves it (ux R2).
    fun fieldAreas(width: Float, height: Float, visible: Int? = null): List<Rect> {
        val clearance = when {
            space == null -> marginPx
            visible == null -> space.marginAround(lineHeight, marginPx)
            else -> space.marginAround(lineHeight, marginPx, visible)
        }
        return listOf(
            Rect(0f, 0f, width, height),
            Rect(0f, lineTop - marginPx, width, height),
            Rect(0f, lineTop - clearance, width, lineTop + lineHeight + clearance)
        )
    }
    suspend fun bringFieldIntoView(width: Float, height: Float) =
        requester.bringNestedIntoView(*fieldAreas(width, height).toTypedArray())
    LaunchedEffect(focused, size) {
        if (!focused || size == IntSize.Zero) return@LaunchedEffect
        bringFieldIntoView(size.width.toFloat(), size.height.toFloat())
        follow.onBroughtIntoView()
    }
    LaunchedEffect(inViewRequests) {
        if (inViewRequests == 0) return@LaunchedEffect
        withFrameNanos { }
        if (size != IntSize.Zero) bringFieldIntoView(size.width.toFloat(), size.height.toFloat())
    }
    // In a typing space the page keeps the field in view as the keyboard grows, in the same layout pass (BRD D194).
    LaunchedEffect(focused, space) {
        if (!focused || space == null) return@LaunchedEffect
        space.keepInView(
            KeptField({ placed.coordinates }, { follow.isFollowing }) { visible ->
                fieldAreas(size.width.toFloat(), size.height.toFloat(), visible)
            }
        )
    }
    // The line being typed is the input line, laid out in the field's text style (BRD D192, ux R5).
    LaunchedEffect(focused, space, lineHeight) {
        if (!focused || space == null) return@LaunchedEffect
        space.reportLine { lineHeight }
    }
    LaunchedEffect(focused) {
        if (!focused) return@LaunchedEffect
        follow.followKeyboard({ keyboard.getBottom(density) }) {
            bringFieldIntoView(size.width.toFloat(), size.height.toFloat())
        }
    }
    field(
        Modifier
            .bringIntoViewRequester(requester)
            .onGloballyPositioned {
                size = it.size
                placed.coordinates = it
                val line = Rect(0f, lineTop, it.size.width.toFloat(), lineTop + lineHeight)
                follow.onPositioned(keyboard.getBottom(density), it, line)
            },
        interactionSource
    )
}

/**
 * The height a focused single-line field needs to show its input line [margin] clear above and below, which is what
 * [SingleLineFieldInView] asks for at least (ux U1, U7), with the line as laid out ([inputLineHeight]).
 */
@Composable
fun inputLineRoom(margin: Dp = LINE_MARGIN): Dp =
    with(LocalDensity.current) { inputLineHeight().toDp() } + margin * 2

/**
 * The input line's height in px: one line laid out in [LocalTextStyle], the style the field uses, at the screen's density,
 * not that style's line height in sp, which nonlinear font scaling converts to less (BRD D192; 95 against 112 px at 200%).
 */
@Composable
private fun inputLineHeight(): Float {
    val textStyle = LocalTextStyle.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    return remember(textStyle, density, measurer) {
        measurer.measure(text = "A", style = textStyle, density = density).size.height.toFloat()
    }
}

/** Where the field was last placed, for the page to keep it in view: not state, so placing it never recomposes. */
private class Placed {
    var coordinates: LayoutCoordinates? = null
}

private fun TextUnit.orBodyLine(): TextUnit = if (isSp) this else BODY_LINE

/**
 * Asks for each of [areas], each inside the one before, at once: a scrolling parent shows the largest of them that fits
 * its visible area, and the smallest when none does.
 */
private suspend fun BringIntoViewRequester.bringNestedIntoView(vararg areas: Rect) =
    coroutineScope {
        areas.forEach { area -> launch { bringIntoView(area) } }
    }
