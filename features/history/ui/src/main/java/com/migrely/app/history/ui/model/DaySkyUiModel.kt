package com.migrely.app.history.ui.model

enum class DaySkyUiModel {
    NothingLogged,

    /** A past day with attack medication but no attack (BRD D100). */
    MedicationOnly,
    Future,
    Clear,
    Hazy,
    Cloudy,
    Storm,
    Rain
}
