package com.migrely.app.designsystem.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Whether a screen's top bar steps aside while the keyboard is up (BRD D192, EC-UX-74, EC-UX-77; ux R3, R5, R9): when
 * the visible part under the bar, at the keyboard's final height, is shorter than the line being typed. Both come from
 * layout, never from a constant, so 200% text that Android scales to 42.7 dp is judged as it is laid out.
 *
 * Decided once per keyboard opening, at the first moment the keyboard's final height, the visible part and the line are
 * all known, and held until the keyboard is gone: moving the cursor, more lines, a scroll, a message or the keyboard
 * changing height decide nothing. A recreated screen (rotation, font change) starts undecided. With no keyboard inset
 * (hardware or floating keyboard, a split screen) there is nothing to decide, so the bar stays.
 */
internal class TopBarAside {
    var aside by mutableStateOf(false)
        private set

    private var decided = false

    /**
     * [finalKeyboard] and [keyboard] are the keyboard's final and current bottom inset in px; [visible] is the visible
     * part under the bar at the final height, and [line] the line being typed, in px, or null while unknown.
     */
    fun update(finalKeyboard: Int, keyboard: Int, visible: Int?, line: Int?) {
        if (finalKeyboard <= 0 && keyboard <= 0) {
            decided = false
            aside = false
            return
        }
        if (decided || finalKeyboard <= 0 || visible == null || line == null) return
        decided = true
        aside = visible < line
    }
}
