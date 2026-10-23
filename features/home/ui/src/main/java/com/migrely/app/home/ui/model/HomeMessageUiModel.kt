package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One-off messages shown in a snackbar on the home screen. Parcelable, so a message on screen survives rotation. */
sealed interface HomeMessageUiModel : Parcelable {
    @Parcelize
    data class AttackStarted(val attackId: String) : HomeMessageUiModel

    @Parcelize
    data class AttackEnded(val attackId: String) : HomeMessageUiModel

    @Parcelize
    data object Undone : HomeMessageUiModel

    @Parcelize
    data object UndoNotPossible : HomeMessageUiModel

    @Parcelize
    data object StorageFull : HomeMessageUiModel

    @Parcelize
    data class StartMoveRejected(val previousEndEpochMillis: Long) : HomeMessageUiModel

    @Parcelize
    data object EndTimeOutOfRange : HomeMessageUiModel

    @Parcelize
    data object SomethingWentWrong : HomeMessageUiModel
}
