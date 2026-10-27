package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository

/** Removes a medication from the lists; its intakes keep it (BRD EC-MED-01). The request is its id. */
class ArchiveMedicationUseCase(
    private val setMedicationArchivedRepository: SetMedicationArchivedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        setMedicationArchivedRepository.setMedicationArchived(request, archived = true)
}
