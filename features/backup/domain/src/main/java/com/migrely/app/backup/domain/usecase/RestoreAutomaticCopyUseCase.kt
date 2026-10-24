package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.RestoreAutomaticCopyRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * "Restore the copy" (BRD D98), the undo of a "Replace". Like a "Replace" import, records on the phone are saved as a
 * new automatic copy first, keeping the newest three, so a restore can itself be undone; an empty phone saves none,
 * which would only push an older copy out.
 */
class RestoreAutomaticCopyUseCase(
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository,
    private val restoreAutomaticCopyRepository: RestoreAutomaticCopyRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, RestoreResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): RestoreResultDomainModel =
        when (getPhoneRecordsRepository.phoneRecords()) {
            PhoneRecordsDomainModel.None ->
                restoreAutomaticCopyRepository.restoreAutomaticCopy(saveCopyFirst = false)
            PhoneRecordsDomainModel.Some ->
                restoreAutomaticCopyRepository.restoreAutomaticCopy(saveCopyFirst = true)
            PhoneRecordsDomainModel.Failed -> RestoreResultDomainModel.Failed
        }
}
