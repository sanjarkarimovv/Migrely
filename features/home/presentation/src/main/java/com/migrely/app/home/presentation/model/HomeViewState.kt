package com.migrely.app.home.presentation.model

sealed interface HomeViewState {
    data object Loading : HomeViewState

    data class Today(val today: TodayPresentationModel) : HomeViewState

    data class AttackInProgress(val attack: AttackInProgressPresentationModel) : HomeViewState
}
