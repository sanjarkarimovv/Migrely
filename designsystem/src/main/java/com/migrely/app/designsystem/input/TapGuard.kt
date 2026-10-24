package com.migrely.app.designsystem.input

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/**
 * A transparent layer that swallows every touch for [durationMillis] after it appears or [key] changes, then lets
 * touches through. Place it above the content it protects (for example with `Modifier.matchParentSize()` as a later
 * sibling) and below anything that must stay tappable, such as messages. It has no semantics, so screen readers,
 * switch access and keyboards are not affected (BRD EC-UX-18).
 */
@Composable
fun TapGuard(key: Any, durationMillis: Long, modifier: Modifier = Modifier) {
    var armed by remember(key) { mutableStateOf(true) }
    LaunchedEffect(key) {
        delay(durationMillis)
        armed = false
    }
    if (!armed) return
    Box(
        modifier = modifier.pointerInput(key) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                }
            }
        }
    )
}
