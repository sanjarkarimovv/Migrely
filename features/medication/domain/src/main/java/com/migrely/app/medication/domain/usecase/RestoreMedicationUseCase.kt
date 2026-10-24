package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.RestoreMedicationResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository

/** "Undo" after archiving a medication; refused when 50 are active again in the meantime (BRD EC-MED-01, D35). */
class RestoreMedicationUseCase(
    private val countActiveMedicationsRepository: CountActiveMedicationsRepository,
    private val setMedicationArchivedRepository: SetMedicationArchivedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, RestoreMedicationResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: String): RestoreMedicationResultDomainModel =
        when (val count = countActiveMedicationsRepository.activeMedicationCount()) {
            ActiveCountResultDomainModel.Failed -> RestoreMedicationResultDomainModel.Failed
            is ActiveCountResultDomainModel.Counted ->
                if (count.count >= MAXIMUM_ACTIVE_MEDICATIONS) {
                    RestoreMedicationResultDomainModel.LimitReached
                } else {
                    when (setMedicationArchivedRepository.setMedicationArchived(request, false)) {
                        WriteResultDomainModel.Done -> RestoreMedicationResultDomainModel.Restored
                        WriteResultDomainModel.StorageFull ->
                            RestoreMedicationResultDomainModel.StorageFull

                        WriteResultDomainModel.Failed -> RestoreMedicationResultDomainModel.Failed
                    }
                }
        }
}
