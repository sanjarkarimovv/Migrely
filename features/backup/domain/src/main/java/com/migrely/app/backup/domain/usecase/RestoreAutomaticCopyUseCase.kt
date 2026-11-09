package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.PlanRemindersAgainRepository
import com.migrely.app.backup.domain.repository.RestoreAutomaticCopyRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant

/**
 * "Restore the copy" (BRD D98), the undo of a "Replace". The request is when the copy shown to the user was made, so
 * exactly that copy is restored. Like a "Replace" import, records on the phone are saved as a new automatic copy first,
 * keeping the newest three, so a restore can itself be undone; an empty phone saves none, which would only push an
 * older copy out. The reminders are planned again after it, as after an import (design D7).
 */
class RestoreAutomaticCopyUseCase(
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository,
    private val restoreAutomaticCopyRepository: RestoreAutomaticCopyRepository,
    private val planRemindersAgainRepository: PlanRemindersAgainRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Instant, RestoreResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Instant): RestoreResultDomainModel =
        restore(request).also { result ->
            if (result is RestoreResultDomainModel.Restored) {
                planRemindersAgainRepository.planRemindersAgain()
            }
        }

    private fun restore(request: Instant): RestoreResultDomainModel =
        when (getPhoneRecordsRepository.phoneRecords()) {
            PhoneRecordsDomainModel.None ->
                restoreAutomaticCopyRepository.restoreAutomaticCopy(request, saveCopyFirst = false)
            PhoneRecordsDomainModel.Some ->
                restoreAutomaticCopyRepository.restoreAutomaticCopy(request, saveCopyFirst = true)
            PhoneRecordsDomainModel.Failed -> RestoreResultDomainModel.Failed
        }
}
