package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsScreenDomainModel
import com.migrely.app.attack.domain.model.CustomTagsResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.repository.GetActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Loads one attack with the user's active custom tags, for the details screen. The request is the attack id. */
class GetAttackDetailsUseCase(
    private val getAttackDetailsRepository: GetAttackDetailsRepository,
    private val getActiveCustomTagsRepository: GetActiveCustomTagsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, AttackDetailsResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): AttackDetailsResultDomainModel {
        val details = when (val lookup = getAttackDetailsRepository.attackDetails(request)) {
            is AttackDetailsLookupResultDomainModel.Found -> lookup.details
            AttackDetailsLookupResultDomainModel.NotFound ->
                return AttackDetailsResultDomainModel.NotFound
            AttackDetailsLookupResultDomainModel.Failed ->
                return AttackDetailsResultDomainModel.Failed
        }
        val symptoms =
            activeTags(TagKindDomainModel.Symptom) ?: return AttackDetailsResultDomainModel.Failed
        val triggers =
            activeTags(TagKindDomainModel.Trigger) ?: return AttackDetailsResultDomainModel.Failed
        return AttackDetailsResultDomainModel.Loaded(
            AttackDetailsScreenDomainModel(
                details = details,
                customSymptoms = symptoms,
                customTriggers = triggers,
                now = getCurrentTimeRepository.currentTime().instant
            )
        )
    }

    private fun activeTags(kind: TagKindDomainModel) =
        when (val result = getActiveCustomTagsRepository.activeCustomTags(kind)) {
            is CustomTagsResultDomainModel.Loaded -> result.tags
            CustomTagsResultDomainModel.Failed -> null
        }
}
