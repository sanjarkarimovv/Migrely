package com.migrely.app.attack.domain.usecase

import java.time.Duration

/** Times up to this far after the phone's "now" are accepted, for clocks that are slightly off (BRD EC-ATK-03). */
val CLOCK_TOLERANCE: Duration = Duration.ofMinutes(5)
