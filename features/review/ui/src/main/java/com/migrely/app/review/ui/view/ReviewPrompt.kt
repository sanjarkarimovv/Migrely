package com.migrely.app.review.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.migrely.app.review.ui.di.ReviewPromptDependencies
import kotlinx.coroutines.delay

/** How long home must show today's mode on screen before the app asks (BRD D130). */
internal const val SETTLE_MILLIS = 2_000L

/**
 * The request for a rating (BRD §14.4, D130), in the slot home leaves to `:app` in today's mode
 * only, so it never runs in attack mode. It draws nothing: Google Play draws the whole flow. Every
 * time home loads, [refreshKey] changes; once home has been on screen for [SETTLE_MILLIS] it asks
 * whether a request is due. Leaving today's mode removes it and leaving the screen pauses it, and
 * either drops the wait (EC-REV-07).
 */
@Composable
fun ReviewPromptDependencies.ReviewPrompt(refreshKey: Int) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(refreshKey, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            delay(SETTLE_MILLIS)
            reviewPromptViewModel.onEnter()
        }
    }
}
