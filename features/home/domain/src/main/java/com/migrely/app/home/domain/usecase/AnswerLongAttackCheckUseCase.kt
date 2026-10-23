package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AnswerLongAttackResultDomainModel
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerRequestDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.UpdateLongAttackCheckRepository

class AnswerLongAttackCheckUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val updateLongAttackCheckRepository: UpdateLongAttackCheckRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    LongAttackAnswerRequestDomainModel,
    AnswerLongAttackResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: LongAttackAnswerRequestDomainModel
    ): AnswerLongAttackResultDomainModel {
        val attack = when (val lookup = getAttackInProgressRepository.attackInProgress()) {
            is AttackLookupResultDomainModel.InProgress -> lookup.attack
            AttackLookupResultDomainModel.None ->
                return AnswerLongAttackResultDomainModel.NotInProgress

            AttackLookupResultDomainModel.Failed -> return AnswerLongAttackResultDomainModel.Failed
        }
        if (attack.id != request.attackId) return AnswerLongAttackResultDomainModel.NotInProgress
        val check = when (request.answer) {
            LongAttackAnswerDomainModel.StillGoing ->
                attack.longAttackCheck.copy(
                    confirmedAt = getCurrentTimeRepository.currentTime().instant
                )

            LongAttackAnswerDomainModel.DontAskAgain -> attack.longAttackCheck.copy(disabled = true)
        }
        return when (updateLongAttackCheckRepository.updateLongAttackCheck(attack.id, check)) {
            WriteResultDomainModel.Written -> AnswerLongAttackResultDomainModel.Saved
            WriteResultDomainModel.StorageFull -> AnswerLongAttackResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> AnswerLongAttackResultDomainModel.Failed
        }
    }
}
