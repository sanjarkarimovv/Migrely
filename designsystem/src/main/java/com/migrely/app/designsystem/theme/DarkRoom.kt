package com.migrely.app.designsystem.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How many parts of the screen on show stay dark in every theme. The app provides one through [LocalDarkScreens] and
 * keeps the system bars dark while [shown] is above zero, so light status bar icons never sit on attack mode in the
 * light theme.
 */
@Stable
class DarkScreens {
    var shown by mutableIntStateOf(0)
        private set

    internal fun enter() {
        shown++
    }

    internal fun leave() {
        shown--
    }
}

val LocalDarkScreens = staticCompositionLocalOf { DarkScreens() }

/**
 * Keeps [content] dark in every theme while [active]: attack mode and the dark room, because light hurts during an
 * attack (BRD D94, 18.3 B). Dialogs, pickers and messages inside take the dark scheme too. While not [active],
 * [content] has the app's theme, so a part of the screen can change rooms without losing its state.
 */
@Composable
fun MigrelyDarkRoom(active: Boolean = true, content: @Composable () -> Unit) {
    val screens = LocalDarkScreens.current
    DisposableEffect(screens, active) {
        if (active) screens.enter()
        onDispose {
            if (active) screens.leave()
        }
    }
    val contentColor = LocalContentColor.current
    MigrelyTheme(dark = active || LocalMigrelyDark.current) {
        // Text without a colour of its own is light here, not the dark text the light theme's screen gives it.
        CompositionLocalProvider(
            LocalContentColor provides if (active) {
                MaterialTheme.colorScheme.onBackground
            } else {
                contentColor
            },
            content = content
        )
    }
}
