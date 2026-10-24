package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseStatusResultDomainModel
import com.migrely.app.medication.domain.model.OveruseStatusRowDomainModel
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository

/**
 * The standing overuse indicator (BRD EC-MED-06, D59): each category's attack-medication days in the last 30 days
 * against its limit, counted as home's warning counts them (D45). Unlike the warning, nothing quiets it.
 */
class GetOveruseStatusUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAcuteIntakesRepository: GetAcuteIntakesRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, OveruseStatusResultDomainModel>(coroutineContextProvider) {
    private val overuseDayCounter = OveruseDayCounter(getAcuteIntakesRepository)

    override fun executeInBackground(request: Unit): OveruseStatusResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val days = overuseDayCounter.days(now) ?: return OveruseStatusResultDomainModel.Failed
        return OveruseStatusResultDomainModel.Loaded(
            OveruseCategoryDomainModel.entries.mapNotNull { category ->
                days[category]?.let {
                    OveruseStatusRowDomainModel(category, it, category.thresholdDays)
                }
            }
        )
    }
}
