package com.migrely.app.medication.domain.usecase

import java.time.Duration

/** "Did it help?" is asked from 2 hours after an intake until 24 hours after it (BRD MED-F4, D39). */
internal val QUESTION_FROM: Duration = Duration.ofHours(2)
internal val QUESTION_UNTIL: Duration = Duration.ofHours(24)
