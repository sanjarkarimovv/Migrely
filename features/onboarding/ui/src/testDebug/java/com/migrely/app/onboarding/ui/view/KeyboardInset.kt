package com.migrely.app.onboarding.ui.view

import android.os.Build
import android.os.Looper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.time.Duration
import kotlin.math.roundToInt
import org.robolectric.Shadows.shadowOf

/** The keyboard's height in the keyboard tests (BRD D180, design D4). */
internal const val KEYBOARD_DP = 250

/** Draws the test's window edge to edge, as `MainActivity` does, so its screen receives the insets below. */
internal fun ComponentActivity.drawEdgeToEdge() =
    WindowCompat.setDecorFitsSystemWindows(window, false)

/**
 * Gives the window a bottom keyboard inset of [heightDp], or none with 0, as Android does under `adjustResize`: as an
 * `ime` inset from API 30, and inside the system window insets before it, which androidx then reads as `ime`. Call it
 * on the main thread.
 */
@Suppress("DEPRECATION")
internal fun ComponentActivity.setKeyboard(heightDp: Int) {
    val view = window.decorView
    val bottom = (heightDp * view.resources.displayMetrics.density).roundToInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val ime = WindowInsetsCompat.Type.ime()
        val insets = WindowInsetsCompat.Builder()
            .setInsets(ime, Insets.of(0, 0, 0, bottom))
            .setVisible(ime, heightDp > 0)
            .build()
        ViewCompat.dispatchApplyWindowInsets(view, insets)
    } else {
        // Before API 30, under adjustResize, the keyboard comes inside the system window insets the root dispatches,
        // which androidx then reads as ime: every dispatch to the content carries it from now on.
        val content = findViewById<View>(android.R.id.content)
        content.setOnApplyWindowInsetsListener { _, insets ->
            insets.replaceSystemWindowInsets(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                maxOf(insets.systemWindowInsetBottom, bottom)
            )
        }
        content.requestApplyInsets()
        // androidx animates the change itself from the next draw; Compose takes the new value when that ends.
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
    }
}
