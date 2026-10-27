package com.migrely.app.datasource.attack.model

sealed interface AttackStartDataModel {
    data class Started(val attack: AttackDataModel) : AttackStartDataModel

    data class AlreadyInProgress(val attack: AttackDataModel) : AttackStartDataModel
}
