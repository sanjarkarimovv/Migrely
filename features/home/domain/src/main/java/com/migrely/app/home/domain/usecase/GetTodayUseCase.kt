package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.domain.model.TodayResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.repository.GetTodayRepository
import java.time.Duration
import java.time.Instant

/** An attack open this long triggers the "Still going?" check (BRD EC-ATK-02, SAF-F3). */
val LONG_ATTACK_THRESHOLD: Duration = Duration.ofHours(72)

class GetTodayUseCase(
    private val getTodayRepository: GetTodayRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, TodayResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): TodayResultDomainModel =
        when (val result = getTodayRepository.today()) {
            is TodaySnapshotResultDomainModel.Loaded -> TodayResultDomainModel.Loaded(
                result.snapshot.toToday()
            )
            TodaySnapshotResultDomainModel.Failed -> TodayResultDomainModel.Failed
        }

    private fun TodaySnapshotDomainModel.toToday(): TodayDomainModel {
        val attack = attackInProgress
        return when {
            attack != null -> TodayDomainModel.AttackInProgress(
                attack = attack,
                longAttackCheckDue = attack.isLongAttackCheckDue(now)
            )

            endedAttacksTouchingToday.isEmpty() -> TodayDomainModel.Calm

            else -> TodayDomainModel.AttacksEnded(
                count = endedAttacksTouchingToday.size,
                highestRecordedIntensity = endedAttacksTouchingToday.mapNotNull(
                    AttackDomainModel::peakIntensity
                )
                    .maxOrNull(),
                anyIntensityNotRecorded = endedAttacksTouchingToday.any { it.peakIntensity == null }
            )
        }
    }

    private fun AttackDomainModel.isLongAttackCheckDue(now: Instant): Boolean {
        if (longAttackCheck.disabled) return false
        val since = longAttackCheck.confirmedAt ?: start
        return Duration.between(since, now) >= LONG_ATTACK_THRESHOLD
    }
}
