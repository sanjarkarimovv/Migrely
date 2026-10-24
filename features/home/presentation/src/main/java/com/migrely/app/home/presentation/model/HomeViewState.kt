package com.migrely.app.home.presentation.model

sealed interface HomeViewState {
    data object Loading : HomeViewState

    data object Ready : HomeViewState
}
