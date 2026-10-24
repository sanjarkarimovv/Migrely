package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.repository.GetMedicationRepository

/** One medication for the editor; the request is its id. */
class GetMedicationUseCase(
    private val getMedicationRepository: GetMedicationRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, MedicationLookupResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: String): MedicationLookupResultDomainModel =
        getMedicationRepository.medication(request)
}
