package com.migrely.app.history.presentation.model

/**
 * The sky of one calendar day (BRD EC-UX-09); clear only for a day marked pain-free (BRD D47, D49).
 * [MedicationOnly] is a past day with attack medication but no attack: logged, with no sky
 * (BRD D100).
 */
enum class DaySkyPresentationModel {
    NothingLogged,
    MedicationOnly,
    Future,
    Clear,
    Hazy,
    Cloudy,
    Storm,
    Rain
}
