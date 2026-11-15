package com.migrely.app.settings.ui.view

import android.graphics.Insets
import android.os.Build
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets as InsetsCompat
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
            .setInsets(ime, InsetsCompat.of(0, 0, 0, bottom))
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

/**
 * A keyboard moving from [fromDp] to [toDp] as Android 11 and later animate it: the final insets first, then the frames.
 * Drive it from the main thread: [start], then [frame] for each fraction, then [end].
 */
@RequiresApi(Build.VERSION_CODES.R)
internal class KeyboardAnimation(activity: ComponentActivity, fromDp: Int, toDp: Int) {
    private val view = activity.window.decorView
    private val density = view.resources.displayMetrics.density
    private val from = (fromDp * density).roundToInt()
    private val to = (toDp * density).roundToInt()
    private val animation = WindowInsetsAnimation(WindowInsets.Type.ime(), null, DURATION_MILLIS)

    fun start() {
        view.dispatchWindowInsetsAnimationPrepare(animation)
        view.dispatchApplyWindowInsets(keyboard(to))
        view.dispatchWindowInsetsAnimationStart(
            animation,
            WindowInsetsAnimation.Bounds(Insets.NONE, Insets.of(0, 0, 0, maxOf(from, to)))
        )
    }

    fun frame(fraction: Float) {
        animation.fraction = fraction
        view.dispatchWindowInsetsAnimationProgress(
            keyboard((from + (to - from) * fraction).roundToInt()),
            listOf(animation)
        )
    }

    fun end() = view.dispatchWindowInsetsAnimationEnd(animation)

    private fun keyboard(px: Int): WindowInsets = WindowInsets.Builder()
        .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, px))
        .setVisible(WindowInsets.Type.ime(), px > 0)
        .build()

    private companion object {
        const val DURATION_MILLIS = 300L
    }
}
