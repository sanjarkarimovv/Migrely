package com.migrely.app.reminders.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

/** The card says nothing: closing it shows no message and no Undo (BRD D168). */
sealed interface VisitCardPresentationNotification : PresentationNotification
