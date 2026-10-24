package com.migrely.app.backup.presentation.model

sealed interface YourDataViewState {
    data object Idle : YourDataViewState

    /** A file is being written or read; the actions wait until it is done. */
    data object Working : YourDataViewState
}
