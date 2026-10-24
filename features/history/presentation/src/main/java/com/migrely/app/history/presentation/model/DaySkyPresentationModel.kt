package com.migrely.app.history.presentation.model

/** The sky of one calendar day (BRD EC-UX-09); clear only for a day marked pain-free (BRD D47, D49). */
enum class DaySkyPresentationModel {
    NothingLogged,
    Future,
    Clear,
    Hazy,
    Cloudy,
    Storm,
    Rain
}
