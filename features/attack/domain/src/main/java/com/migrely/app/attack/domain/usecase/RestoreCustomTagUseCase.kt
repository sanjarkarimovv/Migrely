package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.RestoreCustomTagRequestDomainModel
import com.migrely.app.attack.domain.model.RestoreCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Brings a removed custom tag back to the user's list: "Undo" after "Removed from your list" (BRD D199, EC-ATK-26).
 * It counts against the limit as an archived match brought back by name does (EC-ATK-12), so a full list refuses it
 * and the tag stays archived.
 */
class RestoreCustomTagUseCase(
    private val countActiveCustomTagsRepository: CountActiveCustomTagsRepository,
    private val setCustomTagArchivedRepository: SetCustomTagArchivedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    RestoreCustomTagRequestDomainModel,
    RestoreCustomTagResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: RestoreCustomTagRequestDomainModel
    ): RestoreCustomTagResultDomainModel =
        when (val count = countActiveCustomTagsRepository.activeCustomTagCount(request.kind)) {
            is CustomTagCountResultDomainModel.Counted ->
                if (count.count >= MAXIMUM_ACTIVE_CUSTOM_TAGS) {
                    RestoreCustomTagResultDomainModel.LimitReached
                } else {
                    restore(request.tagId)
                }

            CustomTagCountResultDomainModel.Failed -> RestoreCustomTagResultDomainModel.Failed
        }

    private fun restore(tagId: String): RestoreCustomTagResultDomainModel =
        when (setCustomTagArchivedRepository.setCustomTagArchived(tagId, archived = false)) {
            WriteResultDomainModel.Written -> RestoreCustomTagResultDomainModel.Restored
            WriteResultDomainModel.StorageFull -> RestoreCustomTagResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> RestoreCustomTagResultDomainModel.Failed
        }
}
