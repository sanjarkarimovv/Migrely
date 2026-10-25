package com.migrely.app.settings.presentation.model

sealed interface DeleteAllDataViewState {
    /** The user can type the confirmation word and delete. */
    data object Confirming : DeleteAllDataViewState

    /** Deleting, or deleted and about to start again: nothing can be done until the app restarts or it fails. */
    data object Deleting : DeleteAllDataViewState
}
