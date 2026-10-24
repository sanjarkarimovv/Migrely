package com.migrely.app.home.ui.model

/** One-off messages shown in a snackbar on the home screen. */
sealed interface HomeMessageUiModel {
    data class AttackStarted(val attackId: String) : HomeMessageUiModel

    data class AttackEnded(val attackId: String) : HomeMessageUiModel

    data object StorageFull : HomeMessageUiModel

    data class StartMoveRejected(val previousEndEpochMillis: Long) : HomeMessageUiModel

    data object EndTimeOutOfRange : HomeMessageUiModel

    data object SomethingWentWrong : HomeMessageUiModel
}
