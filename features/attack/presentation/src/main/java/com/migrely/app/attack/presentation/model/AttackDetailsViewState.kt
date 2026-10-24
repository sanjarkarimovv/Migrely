package com.migrely.app.attack.presentation.model

sealed interface AttackDetailsViewState {
    data object Loading : AttackDetailsViewState

    data class Ready(val details: AttackDetailsPresentationModel) : AttackDetailsViewState

    /** The attack was deleted or never existed. */
    data object NotFound : AttackDetailsViewState
}
