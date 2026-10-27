package com.migrely.app.home.domain.model

sealed interface UndoResultDomainModel {
    data object Undone : UndoResultDomainModel

    data object NotPossible : UndoResultDomainModel

    data object Failed : UndoResultDomainModel
}
