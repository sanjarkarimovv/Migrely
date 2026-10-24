package com.migrely.app.attack.domain.model

sealed interface UpdateAttackNoteResultDomainModel {
    data object Saved : UpdateAttackNoteResultDomainModel

    data object TooLong : UpdateAttackNoteResultDomainModel

    data object StorageFull : UpdateAttackNoteResultDomainModel

    data object Failed : UpdateAttackNoteResultDomainModel
}
