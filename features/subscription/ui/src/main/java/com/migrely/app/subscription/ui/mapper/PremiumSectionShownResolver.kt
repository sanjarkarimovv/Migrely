package com.migrely.app.subscription.ui.mapper

import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionViewStateUiModel

/**
 * What the Premium section shows for each view state (QA #106). A read of the status, as on the
 * resume after a rotation, brings no restore note, so the note of the last "Restore purchases"
 * stays while its status still shows; a new status drops it, since it may no longer hold, and a
 * new restore drops it while it checks. The section itself drops the note when Settings is left,
 * so it stays across a rotation only.
 */
class PremiumSectionShownResolver {
    fun resolve(
        shown: PremiumSectionUiModel?,
        state: PremiumSectionViewStateUiModel
    ): PremiumSectionUiModel? = when (state) {
        PremiumSectionViewStateUiModel.Loading -> shown
        PremiumSectionViewStateUiModel.Checking -> shown?.copy(note = null)
        is PremiumSectionViewStateUiModel.Ready -> {
            val answer = state.section
            if (answer.note == null && shown?.status == answer.status) {
                answer.copy(note = shown.note)
            } else {
                answer
            }
        }
    }
}
