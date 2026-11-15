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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
 * when the keyboard's height changes and when the field's size does, as when an error line appears.
 *
 * Pass the given modifier first in the field's chain and the given interaction source to it. [hasLabel] says where the
 * input line sits: below half a floating label's line, as Material places it, and its content padding.
 */
@Composable
fun SingleLineFieldInView(
    hasLabel: Boolean,
    margin: Dp = LINE_MARGIN,
    field: @Composable (modifier: Modifier, interactionSource: MutableInteractionSource) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val requester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val keyboardBottom = WindowInsets.ime.getBottom(density)
    val labelLine = MaterialTheme.typography.bodySmall.lineHeight.orBodyLine()
    val labelHalf = if (hasLabel) labelLine / 2 else 0.sp
    val lineTop = with(density) {
        labelHalf.toPx() + OutlinedTextFieldDefaults.contentPadding().calculateTopPadding().toPx()
    }
    val lineHeight = with(density) { LocalTextStyle.current.lineHeight.orBodyLine().toPx() }
    val marginPx = with(density) { margin.toPx() }
    LaunchedEffect(focused, keyboardBottom, size) {
        if (!focused || size == IntSize.Zero) return@LaunchedEffect
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        requester.bringNestedIntoView(
            Rect(0f, 0f, width, height),
            Rect(0f, lineTop - marginPx, width, height),
            Rect(0f, lineTop - marginPx, width, lineTop + lineHeight + marginPx)
        )
    }
    field(
        Modifier
            .bringIntoViewRequester(requester)
            .onSizeChanged { size = it },
        interactionSource
    )
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
