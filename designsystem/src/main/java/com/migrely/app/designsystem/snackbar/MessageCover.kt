package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect

/**
 * Where a shown message covers the screen, in root coordinates, or null while none shows: a page's scroll beneath it
 * leaves that part out of what TalkBack's scroll step counts as visible (#255, BRD EC-UX-79). [MessageLayout] makes
 * one for its message and its content; [MigrelySnackbarHost] fills it in. It is read when TalkBack scrolls.
 */
internal class MessageCover {
    var bounds: Rect? = null
}

internal val LocalMessageCover = staticCompositionLocalOf<MessageCover?> { null }
