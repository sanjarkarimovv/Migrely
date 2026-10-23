package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.EndAttackRequestDomainModel
import com.migrely.app.home.domain.model.EndAttackResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.EndAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository

/** Ends the attack in progress now, or at a picked time that must lie between its start and now. */
class EndAttackUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val endAttackRepository: EndAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<EndAttackRequestDomainModel, EndAttackResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: EndAttackRequestDomainModel
    ): EndAttackResultDomainModel {
        val attack = when (val lookup = getAttackInProgressRepository.attackInProgress()) {
            is AttackLookupResultDomainModel.InProgress -> lookup.attack
            AttackLookupResultDomainModel.None -> return EndAttackResultDomainModel.NotInProgress
            AttackLookupResultDomainModel.Failed -> return EndAttackResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime().instant
        val end = request.end ?: now
        if (end.isBefore(attack.start) || end.isAfter(now)) {
            return EndAttackResultDomainModel.EndOutOfRange
        }
        return when (endAttackRepository.endAttack(attack.id, end)) {
            WriteResultDomainModel.Written -> EndAttackResultDomainModel.Ended(attack.id)
            WriteResultDomainModel.StorageFull -> EndAttackResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> EndAttackResultDomainModel.Failed
        }
    }
}
