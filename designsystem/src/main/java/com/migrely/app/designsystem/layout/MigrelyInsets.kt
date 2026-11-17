package com.migrely.app.designsystem.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable

/**
 * What Migrely's screens keep clear of (BRD D180 (3)). The window resizes for the keyboard but draws edge to edge, so
 * each screen pads for these itself.
 */
object MigrelyInsets {
    /**
     * The system bars and the display cutout, never the keyboard: for a screen with no text field, and for a message's
     * fit rule (BRD D178 (6)), so an open or stale keyboard inset moves neither (#145).
     */
    val bars: WindowInsets
        @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

    /**
     * [bars] and the keyboard, the larger of the two at each side: for a screen with a text field, before its scroll, so
     * the scroll's visible part ends at the keyboard's top (BRD D180 (2)).
     */
    val barsAndKeyboard: WindowInsets
        @Composable get() = bars.union(WindowInsets.ime)
}
