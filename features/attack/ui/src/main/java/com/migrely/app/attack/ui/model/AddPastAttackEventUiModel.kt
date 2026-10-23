package com.migrely.app.attack.ui.model

/** What the add screen's notifications ask it to show: a problem with the times or a message. */
sealed interface AddPastAttackEventUiModel {
    data class Problem(val problem: PastAttackProblemUiModel) : AddPastAttackEventUiModel

    data class Message(val message: AttackMessageUiModel) : AddPastAttackEventUiModel
}
