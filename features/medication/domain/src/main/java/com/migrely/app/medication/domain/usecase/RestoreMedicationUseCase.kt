package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository

/** Undoes an archive from its message (BRD EC-UX-16). The request is the medication id. */
class RestoreMedicationUseCase(
    private val setMedicationArchivedRepository: SetMedicationArchivedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        setMedicationArchivedRepository.setMedicationArchived(request, archived = false)
}
