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
 * Adds a pain entry inside the attack (BRD ATK-F4, EC-ATK-09). Reports when it is the first 10 the user has ever
 * recorded, so the screen can show the emergency card (BRD SAF-F2).
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
        val attack = when (
            val lookup = getAttackDetailsRepository.attackDetails(
                request.attackId
            )
        ) {
            is AttackDetailsLookupResultDomainModel.Found -> lookup.details
            AttackDetailsLookupResultDomainModel.NotFound ->
                return AddIntensityPointResultDomainModel.NotFound
            AttackDetailsLookupResultDomainModel.Failed ->
                return AddIntensityPointResultDomainModel.Failed
        }
        if (point.at.isBefore(attack.start)) return AddIntensityPointResultDomainModel.OutsideAttack
        val end = attack.end
        if (end != null) {
            if (point.at.isAfter(end)) return AddIntensityPointResultDomainModel.OutsideAttack
        } else if (point.at.isAfter(
                getCurrentTimeRepository.currentTime().instant.plus(CLOCK_TOLERANCE)
            )
        ) {
            return AddIntensityPointResultDomainModel.InFuture
        }
        val firstMaximum = point.value == MAXIMUM_INTENSITY &&
            getMaximumRecordedRepository.maximumRecorded() ==
            MaximumRecordedResultDomainModel.NotRecorded
        return when (addIntensityPointRepository.addIntensityPoint(attack.id, point)) {
            WriteResultDomainModel.Written -> AddIntensityPointResultDomainModel.Added(firstMaximum)
            WriteResultDomainModel.StorageFull -> AddIntensityPointResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> AddIntensityPointResultDomainModel.Failed
        }
    }
}
