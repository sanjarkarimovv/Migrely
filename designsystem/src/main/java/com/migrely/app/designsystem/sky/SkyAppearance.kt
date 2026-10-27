package com.migrely.app.designsystem.sky

/** What a sky shows (BRD 18.3). Each appearance differs by shape as well as colour (BRD EC-UX-15). */
enum class SkyAppearance {
    Calm,

    /** A day the user marked pain-free: a full moon instead of the crescent (BRD EC-UX-09, D49). */
    Clear,
    Hazy,
    Cloudy,
    Storm,
    Rain
}
