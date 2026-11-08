package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AddIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.AddIntensityPointResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.MaximumRecordedResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.AddIntensityPointRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.GetMaximumRecordedRepository
import com.migrely.app.coroutine.CoroutineContextProvider

const val MINIMUM_INTENSITY = 1
const val MAXIMUM_INTENSITY = 10

/**
 * Adds a pain entry inside the attack (BRD ATK-F4, EC-ATK-09). Reports with every outcome but a
 * gone attack whether it is a first 10, so the screen shows the emergency card even when the entry
 * is not saved; a history that cannot be read counts as no 10 yet (BRD SAF-F2, D163).
 */
class AddIntensityPointUseCase(
    private val getAttackDetailsRepository: GetAttackDetailsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getMaximumRecordedRepository: GetMaximumRecordedRepository,
    private val addIntensityPointRepository: AddIntensityPointRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    AddIntensityPointRequestDomainModel,
    AddIntensityPointResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: AddIntensityPointRequestDomainModel
    ): AddIntensityPointResultDomainModel {
        val point = request.point
        require(point.value in MINIMUM_INTENSITY..MAXIMUM_INTENSITY) {
            "Intensity must be 1 to 10, was ${point.value}"
        }
        val firstMaximum = point.value == MAXIMUM_INTENSITY &&
            when (getMaximumRecordedRepository.maximumRecorded()) {
                MaximumRecordedResultDomainModel.Recorded -> false
                MaximumRecordedResultDomainModel.NotRecorded,
                MaximumRecordedResultDomainModel.Failed -> true
            }
        val attack = when (
            val lookup = getAttackDetailsRepository.attackDetails(
                request.attackId
            )
        ) {
            is AttackDetailsLookupResultDomainModel.Found -> lookup.details
            AttackDetailsLookupResultDomainModel.NotFound ->
                return AddIntensityPointResultDomainModel.NotFound
            AttackDetailsLookupResultDomainModel.Failed ->
                return AddIntensityPointResultDomainModel.Failed(firstMaximum)
        }
        if (point.at.isBefore(attack.start)) {
            return AddIntensityPointResultDomainModel.OutsideAttack(firstMaximum)
        }
        val end = attack.end
        if (end != null) {
            if (point.at.isAfter(end)) {
                return AddIntensityPointResultDomainModel.OutsideAttack(firstMaximum)
            }
        } else if (point.at.isAfter(
                getCurrentTimeRepository.currentTime().instant.plus(CLOCK_TOLERANCE)
            )
        ) {
            return AddIntensityPointResultDomainModel.InFuture(firstMaximum)
        }
        return when (addIntensityPointRepository.addIntensityPoint(attack.id, point)) {
            WriteResultDomainModel.Written -> AddIntensityPointResultDomainModel.Added(firstMaximum)
            WriteResultDomainModel.StorageFull ->
                AddIntensityPointResultDomainModel.StorageFull(firstMaximum)
            WriteResultDomainModel.Failed -> AddIntensityPointResultDomainModel.Failed(firstMaximum)
        }
    }
}
