package com.migrely.app.designsystem.input

import android.graphics.Insets as PlatformInsets
import android.os.Build
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
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

/**
 * The keyboard's own animation from [from] to [to] px, as Android runs it from API 30: the end state is reported first,
 * which androidx keeps as the target, then each frame's inset. [idle] waits for the screen after each step.
 */
internal class KeyboardAnimation(
    private val activity: ComponentActivity,
    private val statusPx: Int,
    private val from: Int,
    private val to: Int,
    private val idle: () -> Unit
) {
    private val decor get() = activity.window.decorView
    private val animation = WindowInsetsAnimation(WindowInsets.Type.ime(), null, ANIMATION_MILLIS)

    private fun insets(keyboard: Int): WindowInsets = WindowInsets.Builder()
        .setInsets(WindowInsets.Type.statusBars(), PlatformInsets.of(0, statusPx, 0, 0))
        .setVisible(WindowInsets.Type.statusBars(), true)
        .setInsets(WindowInsets.Type.ime(), PlatformInsets.of(0, 0, 0, keyboard))
        .setVisible(WindowInsets.Type.ime(), keyboard > 0)
        .build()

    fun start() {
        activity.runOnUiThread {
            decor.dispatchWindowInsetsAnimationPrepare(animation)
            decor.dispatchApplyWindowInsets(insets(to))
            decor.dispatchWindowInsetsAnimationStart(
                animation,
                WindowInsetsAnimation.Bounds(
                    PlatformInsets.NONE,
                    PlatformInsets.of(0, 0, 0, maxOf(from, to))
                )
            )
        }
        idle()
    }

    fun progress(fraction: Float) {
        activity.runOnUiThread {
            animation.fraction = fraction
            val keyboard = from + ((to - from) * fraction).toInt()
            decor.dispatchWindowInsetsAnimationProgress(insets(keyboard), listOf(animation))
        }
        idle()
    }

    fun end() {
        activity.runOnUiThread { decor.dispatchWindowInsetsAnimationEnd(animation) }
        idle()
    }

    private companion object {
        const val ANIMATION_MILLIS = 250L
    }
}
