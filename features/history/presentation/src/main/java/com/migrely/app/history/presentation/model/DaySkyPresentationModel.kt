package com.migrely.app.history.presentation.model

/** The sky of one calendar day (BRD EC-UX-09). The clear sky arrives with day markers (BRD D29). */
enum class DaySkyPresentationModel {
    NothingLogged,
    Future,
    Hazy,
    Cloudy,
    Storm,
    Rain
}
