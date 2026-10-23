package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.Immutable

/** The kind of a snackbar message. Each kind has its own icon shape, so it is never told apart by colour alone. */
enum class SnackbarTone {
    /** Something the user did worked, such as starting an attack. Shown with a check mark. */
    Confirmation,

    /** Neutral information, such as why a time was not accepted. Shown with an "i". */
    Information,

    /** Something failed and the user may need to act. Shown with an "!" and announced at once. */
    Error
}

/** An action at the end of a snackbar, such as "Undo". TalkBack reads [spokenLabel] instead of [label]. */
@Immutable
data class SnackbarAction(val label: String, val spokenLabel: String, val onClick: () -> Unit)
