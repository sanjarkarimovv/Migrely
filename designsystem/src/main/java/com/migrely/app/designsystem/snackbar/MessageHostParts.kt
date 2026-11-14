package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The height [MigrelySnackbarHost]'s tap guard covers: the message shown, and for a replacement the taller of it and the
 * one it replaced, so a tap on the strip a shorter message has just uncovered still reaches nothing (ux F2). It stays
 * after the message goes, so its place stays guarded while the fade ends.
 */
internal class ReplacementGuard {
    private var current: Any? = null
    private var shownHeight by mutableStateOf(0.dp)
    private var replacedHeight by mutableStateOf(0.dp)

    val height: Dp get() = maxOf(shownHeight, replacedHeight)

    /** Told of every message the host is given, before the guard is drawn. */
    fun target(message: Any?) {
        if (message == current) return
        replacedHeight = if (current != null && message != null) shownHeight else 0.dp
        current = message
    }

    fun measured(message: Any, height: Dp) {
        if (message == current) shownHeight = height
    }
}

/**
 * Where the message ends, in root pixels, or 0 while none shows. It follows the animated area, so it grows with an
 * arrival and shrinks only after a fade out; during a replacement it is never above the new message's own bottom, so a
 * taller message pushes what follows it in the same frame, while a shorter one lets the area ease up (ux F1).
 */
internal class MessageBottom {
    private var current: Any? = null
    private var replacing = false
    private var areaTop = 0f
    private var areaHeight = 0
    private var messageHeight = 0

    val value: Float
        get() = when {
            areaHeight == 0 -> 0f
            replacing -> areaTop + maxOf(areaHeight, messageHeight)
            else -> areaTop + areaHeight
        }

    fun target(message: Any?) {
        if (message == current) return
        replacing = current != null && message != null
        current = message
    }

    fun area(top: Float, height: Int) {
        areaTop = top
        areaHeight = height
    }

    /** Whether the bottom moved because the message shown has a new height. */
    fun measured(message: Any, height: Int): Boolean {
        if (message != current || height == messageHeight) return false
        messageHeight = height
        return replacing && areaHeight > 0
    }
}

/**
 * Keeps keyboard and switch focus off a message that is leaving, and moves focus off the focused message when it
 * leaves or is replaced, to where nothing acts on Enter (ux F8).
 */
internal class MessageFocus(
    private val focusManager: FocusManager,
    private val park: MessageFocusPark?
) {
    private var inside = false

    fun modifier(leaving: Boolean): Modifier = if (leaving) {
        Modifier
            .clearAndSetSemantics {}
            .focusProperties { onEnter = { cancelFocusChange() } }
            .focusGroup()
    } else {
        Modifier.onFocusChanged { inside = it.hasFocus }
    }

    fun changed() {
        if (!inside) return
        inside = false
        // Android gives focus back to the first control as soon as it is cleared while a keyboard is in use, so with a
        // MessageLayout focus goes to its parking place before the screen's first stop instead.
        if (park == null || !park.park()) focusManager.clearFocus(force = true)
    }
}

/**
 * Remembers [message] between compositions and, in the composition that drops or swaps the focused message, moves
 * focus off it before its node goes, so focus is never passed on to another control by the system.
 */
@Composable
internal fun rememberMessageFocus(message: Any?): MessageFocus {
    val focusManager = LocalFocusManager.current
    val park = LocalMessageFocusPark.current
    val focus = remember(focusManager, park) { MessageFocus(focusManager, park) }
    val last = remember { arrayOf(message) }
    if (last[0] != message) {
        last[0] = message
        focus.changed()
    }
    return focus
}
