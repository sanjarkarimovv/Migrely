package com.migrely.app.datasource.attack.model

sealed interface RestoreDataModel {
    data object Restored : RestoreDataModel

    /** The attack is gone, or restoring it would overlap another attack or make two attacks in progress. */
    data object NotPossible : RestoreDataModel
}
