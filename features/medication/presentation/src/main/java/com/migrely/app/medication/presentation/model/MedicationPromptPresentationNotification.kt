package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

/** The card shows no messages of its own: after a failed write it loads again and stays, so the user can retry. */
sealed interface MedicationPromptPresentationNotification : PresentationNotification
