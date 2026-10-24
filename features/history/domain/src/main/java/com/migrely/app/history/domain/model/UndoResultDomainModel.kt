package com.migrely.app.history.domain.model

sealed interface UndoResultDomainModel {
    data object Undone : UndoResultDomainModel

    data object NotPossible : UndoResultDomainModel

    data object Failed : UndoResultDomainModel
}
