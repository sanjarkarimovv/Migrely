package com.migrely.app.attack.ui.model

/** What the ViewModel's notifications ask the screen to show: an inline error, the safety card or a message. */
sealed interface AttackDetailsEventUiModel {
    data class TimeRejected(val error: TimeErrorUiModel) : AttackDetailsEventUiModel

    /** The first 10 the user ever recorded (BRD SAF-F2). */
    data object FirstMaximumRecorded : AttackDetailsEventUiModel

    data class Message(val message: AttackMessageUiModel) : AttackDetailsEventUiModel
}
