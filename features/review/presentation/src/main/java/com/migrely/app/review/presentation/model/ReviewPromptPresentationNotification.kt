package com.migrely.app.review.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

/** The review prompt shows nothing of its own: Google Play draws the whole flow (BRD D129). */
sealed interface ReviewPromptPresentationNotification : PresentationNotification
