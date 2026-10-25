package com.migrely.app.settings.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface DeleteAllDataPresentationNotification : PresentationNotification {
    /** Deleting stopped partway; trying again finishes it. */
    data object NotDeleted : DeleteAllDataPresentationNotification
}
