package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

/** The indicator shows no messages: when the status cannot be read it stays hidden, like the warning. */
sealed interface OveruseIndicatorPresentationNotification : PresentationNotification
