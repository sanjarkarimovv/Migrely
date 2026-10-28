package com.migrely.app.subscription.ui.mapper

import com.migrely.app.subscription.ui.model.PremiumSectionUiModel

/**
 * What the Premium section shows when the status is read again (QA #106). A read on entering or
 * resuming, as after a rotation, brings no restore note, so the note of the last "Restore
 * purchases" stays while its status still shows; a new status drops it, since it may no longer
 * hold.
 */
class PremiumSectionShownResolver {
    fun resolve(
        shown: PremiumSectionUiModel?,
        answer: PremiumSectionUiModel
    ): PremiumSectionUiModel = if (answer.note == null && shown?.status == answer.status) {
        answer.copy(note = shown.note)
    } else {
        answer
    }
}
