package com.migrely.app.history.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

/** The month card shows no messages of its own: a failure only hides it (MonthSkyViewState.Unavailable). */
sealed interface MonthSkyPresentationNotification : PresentationNotification
