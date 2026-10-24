package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.UpdateAttackNoteRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** The longest note an attack can hold (BRD EC-ATK-13). */
const val MAXIMUM_NOTE_LENGTH = 1_000

class UpdateAttackNoteUseCase(
    private val updateAttackNoteRepository: UpdateAttackNoteRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    UpdateAttackNoteRequestDomainModel,
    UpdateAttackNoteResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: UpdateAttackNoteRequestDomainModel
    ): UpdateAttackNoteResultDomainModel {
        if (request.note.length >
            MAXIMUM_NOTE_LENGTH
        ) {
            return UpdateAttackNoteResultDomainModel.TooLong
        }
        return when (updateAttackNoteRepository.updateAttackNote(request.attackId, request.note)) {
            WriteResultDomainModel.Written -> UpdateAttackNoteResultDomainModel.Saved
            WriteResultDomainModel.StorageFull -> UpdateAttackNoteResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> UpdateAttackNoteResultDomainModel.Failed
        }
    }
}
