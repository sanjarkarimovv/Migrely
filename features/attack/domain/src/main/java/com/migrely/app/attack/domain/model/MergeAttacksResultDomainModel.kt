package com.migrely.app.attack.domain.model

sealed interface MergeAttacksResultDomainModel {
    /** The attacks are now one attack, [attackId] (BRD D27). */
    data class Merged(val attackId: String) : MergeAttacksResultDomainModel

    /**
     * Nothing overlapped any more, so the times were stored on [attackId]: a new attack as picked, an edited one
     * widened to also cover its current times and pain entries, never shrunk.
     */
    data class Saved(val attackId: String) : MergeAttacksResultDomainModel

    data class InFuture(val field: TimeFieldDomainModel) : MergeAttacksResultDomainModel

    data object EndBeforeStart : MergeAttacksResultDomainModel

    /** The edited attack no longer exists. */
    data object NotFound : MergeAttacksResultDomainModel

    data object StorageFull : MergeAttacksResultDomainModel

    data object Failed : MergeAttacksResultDomainModel
}
