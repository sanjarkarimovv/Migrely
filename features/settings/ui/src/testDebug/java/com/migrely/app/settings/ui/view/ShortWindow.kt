package com.migrely.app.settings.ui.view

import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * QA's API 36 Gboard cells for #245 (comment 6970307531), at density 2.625 and 200% landscape, in px: the status bar,
 * and the keyboard's top in the 360 dp window (`wm size 945x2100`) and in the 411 dp one. The same file sits in each UI
 * module's tests, which share no test module.
 */
internal const val STATUS_PX = 63
internal const val KEYBOARD_TOP_360_PX = 318
internal const val KEYBOARD_TOP_411_PX = 394

/** The Robolectric qualifiers of those two windows, landscape, density 2.625. */
internal const val WINDOW_360 = "w800dp-h360dp-land-420dpi"
internal const val WINDOW_411 = "w914dp-h411dp-land-420dpi"

/**
 * Gives the window (API 30 and up) a status bar of [STATUS_PX] and a keyboard whose top lies at [keyboardTopPx], or no
 * keyboard with null. Call it on the main thread, after the content is attached.
 */
internal fun ComponentActivity.setShortWindowInsets(keyboardTopPx: Int?) {
    val view = window.decorView
    val keyboard = keyboardTopPx?.let { view.height - it } ?: 0
    val ime = WindowInsetsCompat.Type.ime()
    val status = WindowInsetsCompat.Type.statusBars()
    val insets = WindowInsetsCompat.Builder()
        .setInsets(status, Insets.of(0, STATUS_PX, 0, 0))
        .setVisible(status, true)
        .setInsets(ime, Insets.of(0, 0, 0, keyboard))
        .setVisible(ime, keyboard > 0)
        .build()
    ViewCompat.dispatchApplyWindowInsets(view, insets)
}
