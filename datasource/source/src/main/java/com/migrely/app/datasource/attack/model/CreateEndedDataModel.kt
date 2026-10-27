package com.migrely.app.datasource.attack.model

sealed interface CreateEndedDataModel {
    data class Created(val attackId: String) : CreateEndedDataModel

    /** Nothing was stored: another attack overlaps the times. */
    data object Overlapping : CreateEndedDataModel
}
