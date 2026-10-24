package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository

/** The active medications for My medications, most recently used first, then by name (BRD D35). */
class GetMedicationsUseCase(
    private val getActiveMedicationsRepository: GetActiveMedicationsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, MedicationsResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): MedicationsResultDomainModel =
        getActiveMedicationsRepository.activeMedications()
}
