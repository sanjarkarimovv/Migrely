package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.IntensityLevelDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.RecordIntensityResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.AddIntensityPointRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.ReplaceLatestIntensityPointRepository
import java.time.Duration

/** A new choice this soon after the previous one is treated as a correction, not a change in pain. */
val INTENSITY_CORRECTION_WINDOW: Duration = Duration.ofMinutes(2)

class RecordIntensityUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val addIntensityPointRepository: AddIntensityPointRepository,
    private val replaceLatestIntensityPointRepository: ReplaceLatestIntensityPointRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<IntensityLevelDomainModel, RecordIntensityResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: IntensityLevelDomainModel
    ): RecordIntensityResultDomainModel {
        val attack = when (val lookup = getAttackInProgressRepository.attackInProgress()) {
            is AttackLookupResultDomainModel.InProgress -> lookup.attack
            AttackLookupResultDomainModel.None ->
                return RecordIntensityResultDomainModel.NotInProgress

            AttackLookupResultDomainModel.Failed -> return RecordIntensityResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime().instant
        val point = IntensityPointDomainModel(at = now, value = request.value)
        val latest = attack.latestIntensityPoint
        val isCorrection =
            latest != null && Duration.between(latest.at, now) < INTENSITY_CORRECTION_WINDOW
        val result = if (isCorrection) {
            replaceLatestIntensityPointRepository.replaceLatestIntensityPoint(attack.id, point)
        } else {
            addIntensityPointRepository.addIntensityPoint(attack.id, point)
        }
        return when (result) {
            WriteResultDomainModel.Written -> RecordIntensityResultDomainModel.Recorded
            WriteResultDomainModel.StorageFull -> RecordIntensityResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> RecordIntensityResultDomainModel.Failed
        }
    }
}
