package com.migrely.app.datasource.attack.model

sealed interface ReopenDataModel {
    data object Reopened : ReopenDataModel

    /** Open-ended again, the attack would overlap another one, or it no longer exists. */
    data object NotPossible : ReopenDataModel
}
