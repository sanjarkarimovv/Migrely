package com.migrely.app.medication.presentation.model

/** History's standing overuse indicator (BRD EC-MED-06, D59). */
sealed interface OveruseIndicatorViewState {
    /** No attack medication in the last 30 days, or the status cannot be read. */
    data object Hidden : OveruseIndicatorViewState

    data class Shown(val rows: List<OveruseIndicatorRowPresentationModel>) :
        OveruseIndicatorViewState
}
