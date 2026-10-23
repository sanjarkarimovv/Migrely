package com.migrely.app.attack.presentation.model

sealed interface AddPastAttackViewState {
    data object Loading : AddPastAttackViewState

    /** The screen can show the picked times; [draft] is where they start. */
    data class Ready(val draft: PastAttackDraftPresentationModel) : AddPastAttackViewState
}
