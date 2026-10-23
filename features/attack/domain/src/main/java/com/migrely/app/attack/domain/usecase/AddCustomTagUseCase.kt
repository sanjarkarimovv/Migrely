package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AddCustomTagRequestDomainModel
import com.migrely.app.attack.domain.model.AddCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.CreateCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.CustomTagLookupResultDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.CreateCustomTagRepository
import com.migrely.app.attack.domain.repository.FindCustomTagRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.util.Locale

const val MAXIMUM_TAG_NAME_LENGTH = 40
const val MAXIMUM_ACTIVE_CUSTOM_TAGS = 100

/**
 * Adds the user's own symptom or trigger and selects it on the attack (BRD ATK-F5, EC-ATK-12). A name that already
 * exists, ignoring case and surrounding spaces, reuses that tag and brings it back if it was archived.
 */
@Suppress("LongParameterList")
class AddCustomTagUseCase(
    private val findCustomTagRepository: FindCustomTagRepository,
    private val countActiveCustomTagsRepository: CountActiveCustomTagsRepository,
    private val createCustomTagRepository: CreateCustomTagRepository,
    private val setCustomTagArchivedRepository: SetCustomTagArchivedRepository,
    private val selectAttackTagRepository: SelectAttackTagRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    AddCustomTagRequestDomainModel,
    AddCustomTagResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: AddCustomTagRequestDomainModel
    ): AddCustomTagResultDomainModel {
        val name = request.name.trim()
        if (name.isEmpty() ||
            name.length > MAXIMUM_TAG_NAME_LENGTH
        ) {
            return AddCustomTagResultDomainModel.Invalid
        }
        val nameKey = name.lowercase(Locale.ROOT)
        val tag = when (val existing = findCustomTagRepository.customTag(request.kind, nameKey)) {
            is CustomTagLookupResultDomainModel.Found -> when (
                val reused = reuse(request.kind, existing.tag)
            ) {
                is CreateOutcome.Created -> reused.tag
                is CreateOutcome.Stopped -> return reused.result
            }

            CustomTagLookupResultDomainModel.None -> when (
                val created = create(
                    request.kind,
                    name,
                    nameKey
                )
            ) {
                is CreateOutcome.Created -> created.tag
                is CreateOutcome.Stopped -> return created.result
            }

            CustomTagLookupResultDomainModel.Failed -> return AddCustomTagResultDomainModel.Failed
        }
        return when (
            selectAttackTagRepository.selectAttackTag(
                request.attackId,
                tag.toAttackTag(),
                selected = true
            )
        ) {
            WriteResultDomainModel.Written -> AddCustomTagResultDomainModel.Added(tag)
            WriteResultDomainModel.StorageFull -> AddCustomTagResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> AddCustomTagResultDomainModel.Failed
        }
    }

    /** An archived match comes back to the list, which counts against the limit like a new tag (BRD EC-ATK-12). */
    private fun reuse(kind: TagKindDomainModel, tag: CustomTagDomainModel): CreateOutcome {
        if (!tag.archived) return CreateOutcome.Created(tag)
        limitReached(kind)?.let { return CreateOutcome.Stopped(it) }
        return when (
            setCustomTagArchivedRepository.setCustomTagArchived(tag.id, archived = false)
        ) {
            WriteResultDomainModel.Written -> CreateOutcome.Created(tag.copy(archived = false))
            WriteResultDomainModel.StorageFull ->
                CreateOutcome.Stopped(AddCustomTagResultDomainModel.StorageFull)
            WriteResultDomainModel.Failed ->
                CreateOutcome.Stopped(AddCustomTagResultDomainModel.Failed)
        }
    }

    /** What stops adding when the active list of [kind] is full or cannot be counted; null when there is room. */
    private fun limitReached(kind: TagKindDomainModel): AddCustomTagResultDomainModel? =
        when (val count = countActiveCustomTagsRepository.activeCustomTagCount(kind)) {
            is CustomTagCountResultDomainModel.Counted ->
                AddCustomTagResultDomainModel.LimitReached.takeIf {
                    count.count >=
                        MAXIMUM_ACTIVE_CUSTOM_TAGS
                }

            CustomTagCountResultDomainModel.Failed -> AddCustomTagResultDomainModel.Failed
        }

    private fun create(kind: TagKindDomainModel, name: String, nameKey: String): CreateOutcome {
        limitReached(kind)?.let { return CreateOutcome.Stopped(it) }
        return when (val created = createCustomTagRepository.createCustomTag(kind, name, nameKey)) {
            is CreateCustomTagResultDomainModel.Created -> CreateOutcome.Created(created.tag)
            CreateCustomTagResultDomainModel.StorageFull ->
                CreateOutcome.Stopped(AddCustomTagResultDomainModel.StorageFull)

            CreateCustomTagResultDomainModel.Failed -> CreateOutcome.Stopped(
                AddCustomTagResultDomainModel.Failed
            )
        }
    }

    private fun CustomTagDomainModel.toAttackTag(): AttackTagDomainModel = when (kind) {
        TagKindDomainModel.Symptom -> AttackTagDomainModel.Symptom(SymptomDomainModel.Custom(this))
        TagKindDomainModel.Trigger -> AttackTagDomainModel.Trigger(TriggerDomainModel.Custom(this))
    }

    private sealed interface CreateOutcome {
        data class Created(val tag: CustomTagDomainModel) : CreateOutcome

        data class Stopped(val result: AddCustomTagResultDomainModel) : CreateOutcome
    }
}
