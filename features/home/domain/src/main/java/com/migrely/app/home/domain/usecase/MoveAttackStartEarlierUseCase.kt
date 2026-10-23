package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.MoveStartResultDomainModel
import com.migrely.app.home.domain.model.PreviousEndResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetPreviousAttackEndRepository
import com.migrely.app.home.domain.repository.UpdateAttackStartRepository
import java.time.Duration

/** Moves the start of the attack in progress earlier by the requested duration. */
class MoveAttackStartEarlierUseCase(
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val getPreviousAttackEndRepository: GetPreviousAttackEndRepository,
    private val updateAttackStartRepository: UpdateAttackStartRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Duration, MoveStartResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Duration): MoveStartResultDomainModel {
        require(!request.isNegative && !request.isZero) { "The start can only move earlier." }
        val attack = when (val lookup = getAttackInProgressRepository.attackInProgress()) {
            is AttackLookupResultDomainModel.InProgress -> lookup.attack
            AttackLookupResultDomainModel.None -> return MoveStartResultDomainModel.NotInProgress
            AttackLookupResultDomainModel.Failed -> return MoveStartResultDomainModel.Failed
        }
        val newStart = attack.start.minus(request)
        when (
            val previous = getPreviousAttackEndRepository.previousAttackEnd(
                attack.id,
                attack.start
            )
        ) {
            is PreviousEndResultDomainModel.Found ->
                if (newStart.isBefore(previous.end)) {
                    return MoveStartResultDomainModel.OverlapsPrevious(previous.end)
                }

            PreviousEndResultDomainModel.None -> Unit
            PreviousEndResultDomainModel.Failed -> return MoveStartResultDomainModel.Failed
        }
        return when (updateAttackStartRepository.updateAttackStart(attack.id, newStart)) {
            WriteResultDomainModel.Written -> MoveStartResultDomainModel.Moved(newStart)
            WriteResultDomainModel.StorageFull -> MoveStartResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> MoveStartResultDomainModel.Failed
        }
    }
}
