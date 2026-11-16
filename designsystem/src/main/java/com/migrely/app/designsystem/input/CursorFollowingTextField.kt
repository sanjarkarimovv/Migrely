package com.migrely.app.designsystem.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

/** The clearance between the cursor line and the scrolling area's edges, the keyboard's top included (ux U1). */
private val CURSOR_MARGIN = 8.dp

/**
 * An outlined text field, drawn as Material's, whose line with the cursor its scrolling parent keeps in view, [margin]
 * clear of the parent's visible edges, while it has focus (BRD D180 (2), EC-UX-39, #141): after typing, a tap, a paste,
 * a cursor move, and when the visible area shrinks as the keyboard opens while that line is in view (KeyboardFollow).
 * The keyboard ending the word it composes changes neither text nor cursor, so it moves nothing.
 * The page scrolls only as far as needed, so a tap on a line already in view moves nothing, and "Remove animations"
 * makes each scroll a jump.
 *
 * The field's own requests to be shown are dropped: Compose's text field asks for its cursor's place from before the
 * tap that focused it (b/216790855), which for a field opening at its end sent the page to that end (ux U4).
 */
@Composable
fun CursorFollowingTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    margin: Dp = CURSOR_MARGIN
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val requester = remember { BringIntoViewRequester() }
    val geometry = remember { CursorGeometry() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val follow = remember { KeyboardFollow() }
    val density = LocalDensity.current
    // The insets object, not its value: the keyboard's frames do not recompose the screen.
    val keyboard = WindowInsets.ime
    val marginPx = with(density) { margin.toPx() }
    val cursor by rememberUpdatedState(value.selection.end)
    // The text and the cursor, not the whole value: a keyboard ending the word it composes changes only the
    // composition, so its new layout, as on closing, never brings the line back (review 7.1 F1, QA 8.1).
    LaunchedEffect(focused, value.text, value.selection) {
        if (!focused) return@LaunchedEffect
        val text = value.text
        // An edit's own layout comes a frame after it.
        val laidOut = snapshotFlow { layout }.first { it?.layoutInput?.text?.text == text }
        val line = geometry.lineOf(laidOut, value.selection.end, marginPx) ?: return@LaunchedEffect
        requester.bringIntoView(line)
        follow.onBroughtIntoView()
    }
    LaunchedEffect(focused) {
        if (!focused) return@LaunchedEffect
        follow.followKeyboard({ keyboard.getBottom(density) }) {
            geometry.lineOf(layout, cursor, marginPx)?.let { requester.bringIntoView(it) }
        }
    }
    val colors = OutlinedTextFieldDefaults.colors()
    val textColor = if (focused) colors.focusedTextColor else colors.unfocusedTextColor
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .bringIntoViewRequester(requester)
                .onGloballyPositioned { geometry.field = it }
                .then(OwnRequestsDropped)
                .defaultMinSize(
                    minWidth = OutlinedTextFieldDefaults.MinWidth,
                    minHeight = OutlinedTextFieldDefaults.MinHeight
                ),
            textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor)),
            cursorBrush = SolidColor(colors.cursorColor),
            interactionSource = interactionSource,
            minLines = minLines,
            onTextLayout = { layout = it },
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value.text,
                    innerTextField = {
                        Box(
                            propagateMinConstraints = true,
                            modifier = Modifier.onGloballyPositioned {
                                geometry.text = it
                                follow.onPositioned(
                                    keyboard.getBottom(density),
                                    geometry.field,
                                    geometry.lineOf(layout, cursor, 0f)
                                )
                            }
                        ) { innerTextField() }
                    },
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = placeholder,
                    supportingText = supportingText,
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors
                        )
                    }
                )
            }
        )
    }
}

/** Where the field and its text are, to place the cursor's line in the field's coordinates. */
private class CursorGeometry {
    var field: LayoutCoordinates? = null
    var text: LayoutCoordinates? = null

    /** The line holding [offset], grown by [margin] above and below, in the field's coordinates. */
    fun lineOf(layout: TextLayoutResult?, offset: Int, margin: Float): Rect? {
        val field = field?.takeIf { it.isAttached } ?: return null
        val text = text?.takeIf { it.isAttached } ?: return null
        if (layout == null || offset > layout.layoutInput.text.length) return null
        val cursor = layout.getCursorRect(offset)
        val origin = field.localPositionOf(text, cursor.topLeft)
        return Rect(
            origin.x,
            origin.y - margin,
            origin.x + cursor.width,
            origin.y + cursor.height + margin
        )
    }
}

/** Ends the requests of what lies inside the field: the field asks for its own lines above. */
private data object OwnRequestsDropped : ModifierNodeElement<OwnRequestsDroppedNode>() {
    override fun create() = OwnRequestsDroppedNode()

    override fun update(node: OwnRequestsDroppedNode) = Unit
}

private class OwnRequestsDroppedNode :
    Modifier.Node(),
    BringIntoViewModifierNode {
    override suspend fun bringIntoView(
        childCoordinates: LayoutCoordinates,
        boundsProvider: () -> Rect?
    ) = Unit
}
