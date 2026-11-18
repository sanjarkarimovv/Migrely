package com.migrely.app.designsystem.input

import android.os.Build
import android.os.Looper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.time.Duration
import org.robolectric.Shadows.shadowOf

/** Draws the test's window edge to edge, as `MainActivity` does, so its screen receives the insets below. */
internal fun ComponentActivity.drawEdgeToEdge() =
    WindowCompat.setDecorFitsSystemWindows(window, false)

/**
 * Gives the window a status bar of [statusPx] and a bottom keyboard inset of [keyboardPx], or none with 0, in pixels,
 * as Android does under `adjustResize`: the keyboard as an `ime` inset from API 30, and inside the system window
 * insets before it, which androidx then reads as `ime`. Call it on the main thread. The same helper, in dp, sits in
 * each UI module's tests, which share no test module.
 */
@Suppress("DEPRECATION")
internal fun ComponentActivity.setInsets(keyboardPx: Int, statusPx: Int = 0) {
    val view = window.decorView
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val ime = WindowInsetsCompat.Type.ime()
        val status = WindowInsetsCompat.Type.statusBars()
        val insets = WindowInsetsCompat.Builder()
            .setInsets(status, Insets.of(0, statusPx, 0, 0))
            .setVisible(status, true)
            .setInsets(ime, Insets.of(0, 0, 0, keyboardPx))
            .setVisible(ime, keyboardPx > 0)
            .build()
        ViewCompat.dispatchApplyWindowInsets(view, insets)
    } else {
        val content = findViewById<View>(android.R.id.content)
        content.setOnApplyWindowInsetsListener { _, insets ->
            insets.replaceSystemWindowInsets(
                insets.systemWindowInsetLeft,
                statusPx,
                insets.systemWindowInsetRight,
                maxOf(insets.systemWindowInsetBottom, keyboardPx)
            )
        }
        content.requestApplyInsets()
        // androidx animates the change itself from the next draw; Compose takes the new value when that ends.
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
    }
}
