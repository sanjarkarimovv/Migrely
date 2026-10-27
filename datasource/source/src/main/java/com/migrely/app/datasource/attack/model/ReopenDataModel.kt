package com.migrely.app.datasource.attack.model

sealed interface ReopenDataModel {
    data object Reopened : ReopenDataModel

    data object AnotherInProgress : ReopenDataModel
}
