package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeRequestDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.RestoreAttackTimesRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Puts back the times and pain entries a time change replaced, unless they would now overlap another attack. */
class UndoTimesChangeUseCase(
    private val getAttackDetailsRepository: GetAttackDetailsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
    private val restoreAttackTimesRepository: RestoreAttackTimesRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    UndoTimesChangeRequestDomainModel,
    UndoTimesChangeResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: UndoTimesChangeRequestDomainModel
    ): UndoTimesChangeResultDomainModel {
        val attack = when (
            val lookup = getAttackDetailsRepository.attackDetails(
                request.attackId
            )
        ) {
            is AttackDetailsLookupResultDomainModel.Found -> lookup.details
            AttackDetailsLookupResultDomainModel.NotFound ->
                return UndoTimesChangeResultDomainModel.NotPossible
            AttackDetailsLookupResultDomainModel.Failed ->
                return UndoTimesChangeResultDomainModel.Failed
        }
        val end = if (attack.isInProgress) null else request.end
        val rangeEnd = end ?: maxOf(getCurrentTimeRepository.currentTime().instant, request.start)
        when (
            findOverlappingAttacksRepository.overlappingAttacks(
                attack.id,
                request.start,
                rangeEnd
            )
        ) {
            is OverlapLookupResultDomainModel.Found ->
                return UndoTimesChangeResultDomainModel.NotPossible
            OverlapLookupResultDomainModel.None -> Unit
            OverlapLookupResultDomainModel.Failed -> return UndoTimesChangeResultDomainModel.Failed
        }
        return when (
            restoreAttackTimesRepository.restoreAttackTimes(
                attack.id,
                request.start,
                end,
                request.points
            )
        ) {
            WriteResultDomainModel.Written -> UndoTimesChangeResultDomainModel.Restored
            WriteResultDomainModel.StorageFull -> UndoTimesChangeResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> UndoTimesChangeResultDomainModel.Failed
        }
    }
}
