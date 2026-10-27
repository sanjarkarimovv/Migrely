package com.migrely.app.attack.domain.model

sealed interface UndoTimesChangeResultDomainModel {
    data object Restored : UndoTimesChangeResultDomainModel

    /** The old times now overlap another attack, or the attack is gone. */
    data object NotPossible : UndoTimesChangeResultDomainModel

    data object StorageFull : UndoTimesChangeResultDomainModel

    data object Failed : UndoTimesChangeResultDomainModel
}
